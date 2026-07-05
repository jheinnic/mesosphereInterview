package info.jchein.mesosphere.elevator.control.manifest;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.control.IPassengerManifest;
import info.jchein.mesosphere.elevator.control.event.WeightLoadUpdated;
import info.jchein.mesosphere.elevator.monitor.model.PendingDropOff;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public abstract class PassengerManifest
implements IPassengerManifest
{
   private final int numFloors;
   private double incomingLoad;
   private double outgoingLoad;
   private double currentLoad;
   private ArrayList<ScheduledStop> scheduledStops;

   // Monotonic counter incremented at every direction reversal.
   // Even values = travelling up, odd values = travelling down.
   // This lets stops be queued for the next pass in either direction while the
   // elevator is still completing the current pass.
   private int currentTravelIndex;

   private int currentFloor;
   private ScheduledPickUp currentPickup;
   private ScheduledDropOff currentDrop;
   private ITravelGraph currentTravelGraph;
   private ITravelGraph nextTravelGraph;


   public PassengerManifest(int numFloors)
   {
      this.numFloors = numFloors;
      this.incomingLoad = 0;
      this.outgoingLoad = 0;
      this.currentLoad = 0;
      this.currentFloor = 0;
      this.currentTravelIndex = 0;
      this.scheduledStops = new ArrayList<ScheduledStop>(3 * numFloors);
      this.currentTravelGraph = null;
      this.nextTravelGraph = null;
      this.currentPickup = null;
      this.currentDrop = null;

      // Sentinel reversal at the top floor ends the first (upward) pass.
      this.scheduledStops.add(new ScheduledReversal(this.numFloors - 1, 0));
   }


   abstract protected ITravelGraph allocateTravelGraph(DirectionOfTravel direction);

   @Override
   public DirectionOfTravel getCurrentDirection() {
      return (this.currentTravelIndex % 2 == 0) ? DirectionOfTravel.GOING_UP : DirectionOfTravel.GOING_DOWN;
   }

   @Override
   public DirectionOfTravel getNextDirection() {
      final DirectionOfTravel nextDirection;
      if (this.currentPickup != null) {
         nextDirection = this.currentPickup.getOutbound();
      } else if (this.scheduledStops.size() > 1) {
         // The ScheduledReversal pseudo-stop node ensures that there is always at least one entry in scheduledStops.
         // Require at leat one entry in addition to the reversal node before electing to have a next direction.
         nextDirection = this.scheduledStops.get(0).getOutbound();
      } else {
         nextDirection = DirectionOfTravel.STOPPED;
      }

      return nextDirection;
   }

   @Override
   public int getCurrentDestination()
   {
      if (this.currentDrop != null) {
         return this.currentDrop.getFloorIndex();
      } else if (this.currentPickup != null) {
         return this.currentPickup.getFloorIndex();
      } else {
         return -1;
      }
   }

   @Override
   public int getCurrentFloor() {
      return this.currentFloor;
   }

   @Override
   public boolean hasCurrentFloorStopRequest() {
      return this.scheduledStops.stream()
         .anyMatch(stop -> stop.getFloorIndex() == this.currentFloor && !stop.isReversal());
   }

   @Override
   public BitSet getFloorStops() {
      return this.toBitSet(this.scheduledStops);
   }

   @Override
   public void trackDoorsOpening()
   {
      final DirectionOfTravel nextDirection = this.getNextDirection();

      if (nextDirection != this.getCurrentDirection()) {
         this.nextTravelGraph = this.allocateTravelGraph(nextDirection);
         this.nextTravelGraph.recordDoorsOpening(this.currentFloor);
      } else {
         this.nextTravelGraph = this.currentTravelGraph;
      }

      this.currentTravelGraph.recordDoorsOpening(this.currentFloor);
   }


   @Override
   public void trackWeightChange(double previousWeight, double weightDelta, double currentWeight)
   {
      // TOOO: Fix weight semantics!
      if (weightDelta < 0) {
         this.outgoingLoad = this.outgoingLoad - weightDelta;
         Preconditions.checkState(this.outgoingLoad <= this.currentLoad);
         // this.currentTravelGraph.recordDisembarked(-1 * weightDelta);
      } else {
         this.incomingLoad = this.incomingLoad + weightDelta;
         // this.nextTravelGraph.recordBoarded(weightDelta);
      }
   }


   @Override
   public void trackDoorsClosed()
   {
      if (this.currentTravelGraph != this.nextTravelGraph) {
         double carryOver = this.currentTravelGraph.getCurrentWeightLoad();
         if (carryOver > 0) {
            log.warn("Passengers still on board at direction reversal reclassifying : {}", carryOver);
            this.incomingLoad += carryOver;
            this.outgoingLoad += carryOver;
         }
      }

      this.currentTravelGraph.recordDisembarked(this.outgoingLoad);
      this.nextTravelGraph.recordBoarded(this.incomingLoad);
      this.currentLoad = this.currentLoad + this.incomingLoad - this.outgoingLoad;
   }


   private void enqueueStop(final ScheduledStop newStop)
   {
      int insertionPoint = Collections.<ScheduledStop> binarySearch(this.scheduledStops, newStop);
      if (insertionPoint < 0) {
         insertionPoint = -(insertionPoint) - 1;
      }
      this.scheduledStops.add(insertionPoint, newStop);

      if ((this.currentDrop != null) && (newStop.compareTo(this.currentDrop) < 0)) {
         // TODO: Midflight destination change!!
         if (newStop.isDropOff()) {
            this.currentDrop = (ScheduledDropOff) newStop;
         } else {
            this.currentDrop = null;
         }
      } else if ((this.currentPickup != null) && (newStop.compareTo(this.currentPickup) < 0)) {
         if (newStop.isPickup()) {
            this.currentPickup = (ScheduledPickUp) newStop;
         }
      }
   }


   @Override
   public void trackDropRequest(final int destinationFloor)
   {
      final ScheduledDropOff newStop = new ScheduledDropOff(destinationFloor, this.currentTravelIndex);
      this.enqueueStop(newStop);
      this.nextTravelGraph.recordDropRequest(destinationFloor);
   }


   /**
    * Computes the travelIndex for a new pickup at {@code floorIndex} in {@code direction}.
    *
    * Rules:
    *   - Same direction as the current pass AND floor is still ahead of us  → current travelIndex
    *   - Same direction as the current pass BUT floor is already behind us  → travelIndex + 2  (skip a full round-trip)
    *   - Opposite direction from the current pass                           → travelIndex + 1  (next pass, opposite parity)
    *
    * "Ahead" means:
    *   - upward pass  (even travelIndex): floorIndex >= currentFloor
    *   - downward pass (odd travelIndex): floorIndex <= currentFloor
    */
   private int travelIndexFor(int floorIndex, DirectionOfTravel direction) {
      Preconditions.checkArgument(direction != DirectionOfTravel.STOPPED);
      final boolean wantUp   = (direction == DirectionOfTravel.GOING_UP);
      final boolean currentUp = (this.currentTravelIndex % 2 == 0);

      if (wantUp == currentUp) {
         // Same direction: determine whether the floor is still reachable this pass.
         final boolean floorIsAhead = currentUp
            ? (floorIndex >= this.currentFloor)   // going up:   ahead = at or above
            : (floorIndex <= this.currentFloor);   // going down: ahead = at or below
         return floorIsAhead ? this.currentTravelIndex : this.currentTravelIndex + 2;
      } else {
         // Opposite direction: earliest opportunity is the very next pass.
         return this.currentTravelIndex + 1;
      }
   }


   @Override
   /**
    * Account for an assigned pickup request.
    *
    * @returns true if accepting the assigned pickup has changed the current destination, requiring the caller to recompute
    *          its braking trajectory.  If the new destination falls beyond the range of safe braking requirements, it is the
    *          caller's responsibility to then revert the change by calling trackDroppedPickup() to reverse the changes made
    *          here.
    */
   public boolean trackAssignedPickup(int floorIndex, DirectionOfTravel direction)
   {
      final int travelIndex = this.travelIndexFor(floorIndex, direction);
      final ScheduledPickUp newStop = new ScheduledPickUp(floorIndex, travelIndex);

      // Pickup assignments can preempt the current destination if they are "on the way" to it.

      boolean changedDestination = true;
      if (this.currentDrop != null) {
         if (newStop.compareTo(this.currentDrop) < 0) {
            // The new pickup is in our direction of travel and we'll reach it before the current drop.  Re-queue the current drop and change destination.
            this.enqueueStop(this.currentDrop);
            if (this.currentPickup != null) {
               this.enqueueStop(this.currentPickup);
            }
            this.currentDrop = null;
            this.currentPickup = newStop;
         } else if ((this.currentDrop.getFloorIndex() == floorIndex) &&
                    (this.getCurrentDirection() == direction) &&
                    (travelIndex == this.currentTravelIndex)) {
            // We just picked up a pickup request for the same floor as the current drop and in the same direction we're already travelling, so we
            // can just add it to the current destination as current pickup.
            Preconditions.checkState(this.currentPickup == null, "We should not be handling a pickup already assigned as next!");
            this.currentPickup = newStop;
            changedDestination = false;
         } else {
            // We picked up a pickup request that is after our current destination.  Queue it.
            int insertionPoint = Collections.<ScheduledStop> binarySearch(this.scheduledStops, newStop);
            if (insertionPoint < 0) insertionPoint = -(insertionPoint) - 1;
            log.info(String.format("Queuing new pickup at index %d.", insertionPoint));
            this.scheduledStops.add(insertionPoint, newStop);
            changedDestination = false;
         }
      } else if (this.currentPickup != null) {
         if (newStop.compareTo(this.currentPickup) < 0) {
            // The new pickup is in our direction of travel and we'll reach it before the current pickup.  Re-queue the old pickup and change destination.
            this.enqueueStop(this.currentPickup);
            this.currentPickup = newStop;
         } else {
            // We picked up a pickup request that is after our current destination.  Queue it.
            int insertionPoint = Collections.<ScheduledStop> binarySearch(this.scheduledStops, newStop);
            if (insertionPoint < 0) insertionPoint = -(insertionPoint) - 1;
            log.info(String.format("Queuing new pickup at index %d.", insertionPoint));
            this.scheduledStops.add(insertionPoint, newStop);
            changedDestination = false;
         }
      } else {
         // We had no previous destination, so this is a defactor new (initial) destination.
         this.currentPickup = newStop;
      }

      this.nextTravelGraph.recordAssignedPickup(floorIndex, direction);
      return changedDestination;
   }


   @Override
   public boolean trackCanceledPickup(int floorIndex, DirectionOfTravel direction)
   {
      final int travelIndex = this.travelIndexFor(floorIndex, direction);
      final ScheduledPickUp searchKey = new ScheduledPickUp(floorIndex, travelIndex);

      boolean changedDestination = false;
      if ((this.currentPickup != null) &&
         (this.currentPickup.getFloorIndex() == floorIndex) &&
         (this.currentPickup.getOutbound() == direction) &&
         (this.currentPickup.travelIndex == travelIndex))
      {
         this.currentPickup = null;
         if (this.currentDrop == null) {
            this.popNextDestination();
            changedDestination = true;
         }
      } else {
         int insertionPoint = Collections.<ScheduledStop> binarySearch(this.scheduledStops, searchKey);
         Preconditions.checkState(insertionPoint >= 0, "Canceled pickup not found in schedule");
         this.scheduledStops.remove(insertionPoint);
      }

      this.nextTravelGraph.recordCancelledPickup(floorIndex, direction);
      return changedDestination;
   }


   @Override
   public boolean trackSlowingThrough(int floorIndex, DirectionOfTravel direction) {
      if ((this.getCurrentDirection() == direction) && (this.getCurrentDestination() == floorIndex)) {
         this.currentFloor = floorIndex;
         return true;
      }

      return false;
   }


   @Override
   public boolean trackTravelThrough(int floorIndex, DirectionOfTravel direction) {
      if ((this.getCurrentDirection() == direction) &&
         (((this.getCurrentDestination() > floorIndex) && (direction == DirectionOfTravel.GOING_UP)) ||
            ((this.getCurrentDestination() < floorIndex) && (direction == DirectionOfTravel.GOING_DOWN)))) {
         this.currentFloor = floorIndex;
         return true;
      }

      return false;
   }


   private void popNextDestination() {
      if (this.scheduledStops.size() > 0) {
         final ScheduledStop nextStop = this.scheduledStops.remove(0);
         if (nextStop.isReversal()) {
            // Consuming a reversal advances the traversal epoch; the next stop belongs to the new pass.
            this.currentTravelIndex++;
            this.popNextDestination();
         } else if (nextStop.isDropOff()) {
            this.currentDrop = (ScheduledDropOff) nextStop;
            if (!this.scheduledStops.isEmpty()) {
               final ScheduledStop peek = this.scheduledStops.get(0);
               if (peek.isPickup() &&
                   peek.getFloorIndex() == this.currentDrop.getFloorIndex() &&
                   peek.travelIndex == this.currentDrop.travelIndex) {
                  this.currentPickup = (ScheduledPickUp) this.scheduledStops.remove(0);
               }
            }
         } else if (nextStop.isPickup()) {
            this.currentPickup = (ScheduledPickUp) nextStop;
         }
      }
   }

   @Override
   public double getCurrentWeightLoad()
   {
      return this.currentLoad + this.incomingLoad - this.outgoingLoad;
   }


   private BitSet toBitSet(final List<ScheduledStop> list)
   {
      final BitSet bitSet = new BitSet();
      list.stream().map(stop -> stop.getFloorIndex()).forEach(bitSet::set);
      return bitSet;
   }


   // -------------------------------------------------------------------------
   // Stop purpose taxonomy
   // -------------------------------------------------------------------------

   enum StopPurpose
   {
      UPWARD_DROP,
      DOWNWARD_DROP,
      UPWARD_CALL,
      DOWNWARD_CALL,
      REVERSE;
   }


   // -------------------------------------------------------------------------
   // Scheduled stop hierarchy
   // -------------------------------------------------------------------------

   public abstract class ScheduledStop
   implements Comparable<ScheduledStop>
   {
      final int floorIndex;
      final StopPurpose purpose;
      final int travelIndex;


      /**
       * The interesting sequences are:
       * -- DOWNWARD_DROP, DOWNWARD_CALL
       * -- UPWARD_DROP, UPWARD_CALL
       * -- UPWARD_DROP, REVERSE, DOWNWARD_CALL  (travelIndex+1 at REVERSE)
       * -- DOWNWARD_DROP, REVERSE, UPWARD_CALL  (travelIndex+1 at REVERSE)
       *
       * @param floorIndex
       * @param purpose
       * @param travelIndex Monotonic counter that changes every time there is a reversal of direction.  All even values go
       *                    up, and all odd values go down.  Stops on different travelIndex values sort by travelIndex alone;
       *                    stops on the same travelIndex sort by floor (ascending for even, descending for odd).
       *                    This allows future-direction stops to be queued while the current pass is still in progress.
       */
      ScheduledStop(int floorIndex, StopPurpose purpose, int travelIndex)
      {
         Preconditions.checkArgument(0 <= floorIndex && floorIndex < PassengerManifest.this.numFloors);
         Preconditions.checkNotNull(purpose);
         Preconditions.checkArgument(travelIndex >= 0);

         this.floorIndex = floorIndex;
         this.purpose = purpose;
         this.travelIndex = travelIndex;
      }

      public StopPurpose getPurpose() {
         return this.purpose;
      }

      public abstract boolean isReversal();

      public abstract boolean isPickup();

      public abstract boolean isDropOff();

      PassengerManifest getManifest()
      {
         return PassengerManifest.this;
      }


      int getFloorIndex() {
         return this.floorIndex;
      }


      protected DirectionOfTravel getOutbound() {
         return (this.travelIndex % 2 == 0) ? DirectionOfTravel.GOING_UP : DirectionOfTravel.GOING_DOWN;
      }


      @Override
      public int compareTo(ScheduledStop o2)
      {
         Preconditions.checkNotNull(o2);
         Preconditions.checkArgument(this.getManifest() == o2.getManifest());

         if (this == o2) { return 0; }

         // Different passes: earlier travelIndex always comes first.
         if (this.travelIndex != o2.travelIndex) {
            return Integer.compare(this.travelIndex, o2.travelIndex);
         }

         // Same pass: even passes ascend by floor, odd passes descend by floor.
         if (this.floorIndex != o2.floorIndex) {
            return (this.travelIndex % 2 == 0)
               ? Integer.compare(this.floorIndex, o2.floorIndex)
               : Integer.compare(o2.floorIndex, this.floorIndex);
         }

         // Same pass, same floor: use stop-type ordering.
         switch(this.getPurpose()) {
            case UPWARD_DROP: {
               switch(o2.getPurpose()) {
                  case UPWARD_CALL: return -1;
                  case UPWARD_DROP: return 0;
                  default: {
                     throw new IllegalStateException(
                        String.format("this is %s, o2 is %s", this.getPurpose(), o2.getPurpose())
                     );
                  }
               }
            }
            case DOWNWARD_DROP: {
               switch(o2.getPurpose()) {
                  case DOWNWARD_CALL: return -1;
                  case DOWNWARD_DROP: return 0;
                  default: {
                     throw new IllegalStateException(
                        String.format("this is %s, o2 is %s", this.getPurpose(), o2.getPurpose())
                     );
                  }
               }
            }
            case UPWARD_CALL: {
               switch(o2.getPurpose()) {
                  case UPWARD_CALL: return 0;
                  case UPWARD_DROP: return 1;
                  case REVERSE: return -1;
                  default: {
                     throw new IllegalStateException(
                        String.format("this is %s, o2 is %s", this.getPurpose(), o2.getPurpose())
                     );
                  }
               }
            }
            case DOWNWARD_CALL: {
               switch(o2.getPurpose()) {
                  case DOWNWARD_CALL: return 0;
                  case DOWNWARD_DROP: return 1;
                  case REVERSE: return -1;
                  default: {
                     throw new IllegalStateException(
                        String.format("this is %s, o2 is %s", this.getPurpose(), o2.getPurpose())
                     );
                  }
               }
            }
            case REVERSE: {
               switch(o2.getPurpose()) {
                  case REVERSE: return 0;
                  case DOWNWARD_CALL: return -1;
                  case UPWARD_CALL: return -1;
                  default: {
                     throw new IllegalStateException(
                        String.format("this is %s, o2 is %s", this.getPurpose(), o2.getPurpose())
                     );
                  }
               }
            }
         }

         throw new IllegalStateException(
             String.format("this is %s, o2 is %s", this, o2)
         );
      }
   }


   // Unified pickup stop — direction encoded by travelIndex parity.
   public class ScheduledPickUp extends ScheduledStop
   {
      public ScheduledPickUp(int floorIndex, int travelIndex)
      {
         super(floorIndex,
               (travelIndex % 2 == 0) ? StopPurpose.UPWARD_CALL : StopPurpose.DOWNWARD_CALL,
               travelIndex);
         if (travelIndex % 2 == 0) {
            Preconditions.checkArgument(floorIndex < PassengerManifest.this.numFloors - 1,
               "Upward pickup floor must not be the top floor");
         } else {
            Preconditions.checkArgument(floorIndex > 0,
               "Downward pickup floor must not be the bottom floor");
         }
      }

      @Override
      public boolean isReversal() { return false; }

      @Override
      public boolean isPickup() { return true; }

      @Override
      public boolean isDropOff() { return false; }

      @Override
      public String toString() {
         return String.format("%s@floor=%d,pass=%d",
            (this.travelIndex % 2 == 0) ? "UP_CALL" : "DOWN_CALL",
            this.floorIndex, this.travelIndex);
      }
   }


   // Drop-off stop — direction encoded by travelIndex parity.
   // Constructor does not validate against current floor position because drops may be
   // pre-scheduled for a future pass before the elevator has reached that position.
   public class ScheduledDropOff extends ScheduledStop
   {
      public ScheduledDropOff(int floorIndex, int travelIndex)
      {
         super(floorIndex,
               (travelIndex % 2 == 0) ? StopPurpose.UPWARD_DROP : StopPurpose.DOWNWARD_DROP,
               travelIndex);
      }


      @Override
      public boolean isReversal() { return false; }

      @Override
      public boolean isPickup() { return false; }

      @Override
      public boolean isDropOff() { return true; }

      @Override
      public String toString() {
         return String.format("%s@floor=%d,pass=%d",
            (this.travelIndex % 2 == 0) ? "UP_DROP" : "DOWN_DROP",
            this.floorIndex, this.travelIndex);
      }
   }


   // Reversal marker — travelIndex is the pass that ends here; consuming it increments currentTravelIndex.
   public class ScheduledReversal extends ScheduledStop
   {
      public ScheduledReversal(int floorIndex, int travelIndex)
      {
         super(floorIndex, StopPurpose.REVERSE, travelIndex);
         Preconditions.checkArgument(
            (floorIndex == 0) || (floorIndex == (PassengerManifest.this.numFloors - 1)) || (travelIndex > 0),
            "Mid-path reversal must not be the initial sentinel");
      }


      @Override
      public boolean isReversal() { return true; }

      @Override
      public boolean isPickup() { return false; }

      @Override
      public boolean isDropOff() { return false; }

      @Override
      protected DirectionOfTravel getOutbound() {
         // After this reversal the next pass has the opposite parity.
         return ((this.travelIndex + 1) % 2 == 0) ? DirectionOfTravel.GOING_UP : DirectionOfTravel.GOING_DOWN;
      }

      @Override
      public String toString() {
         return String.format("REVERSE@floor=%d,pass=%d", this.floorIndex, this.travelIndex);
      }
   }
}
