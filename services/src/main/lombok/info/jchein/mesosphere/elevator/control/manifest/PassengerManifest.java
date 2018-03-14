package info.jchein.mesosphere.elevator.control.manifest;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.control.IPassengerManifest;
import info.jchein.mesosphere.elevator.control.event.WeightLoadUpdated;
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
   private DirectionOfTravel currentDirection;
   private int currentFloor;
   private ScheduledStop currentPickup;
   private ScheduledDropOff currentDrop;


   public PassengerManifest(int numFloors)
   {
      this.numFloors = numFloors;
      this.incomingLoad = 0;
      this.outgoingLoad = 0;
      this.currentLoad = 0;
      this.currentFloor = 0;
      this.scheduledStops = new ArrayList<ScheduledStop>(3 * numFloors);
      this.currentDirection = DirectionOfTravel.GOING_UP;
      this.nextTravelGraph = null;
      this.currentPickup = null;
      this.currentDrop = null;

      this.scheduledStops.add(new ScheduledReversal(this.numFloors - 1));
   }


   abstract protected ITravelGraph allocateTravelGraph(DirectionOfTravel direction);
   
   @Override
   public DirectionOfTravel getCurrentDirection() {
      return this.currentDirection;
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
   public void trackDoorsOpening()
   {
      final DirectionOfTravel nextDirection = this.getNextDirection();
   
      if (nextDirection != this.currentDirection) {
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


   private void enqueueNewStop(final ScheduledStop newStop)
   {
      final int insertionPoint = Collections.<ScheduledStop> binarySearch(this.scheduledStops, newStop);
      this.scheduledStops.add(insertionPoint, newStop);

      if ((this.currentDrop != null) && (newStop.compareTo(this.currentDrop) < 0)) {
         // TODO: Midflight destination change!!
         if (newStop.getPurpose() == StopPurpose.DROP_OFF) {
            this.currentDrop = newStop; 
         } else {
            this.currentDrop = null;
         }
      } else if ((this.currentPickup != null) && (newStop.compareTo(this.currentDrop) < 0)) {
         if (newStop.getPurpose() == StopPurpose.DOWNWARD_CALL) || (newStop.getPurpose() == StopPurpose.UPWARD_CALL)) {
            this.currentPickup = newStop;
         } else {
      }
   }


   @Override
   public void trackDropRequest(final int destinationFloor)
   {
      final ScheduledDropOff newStop = new ScheduledDropOff(destinationFloor);
      final int insertionPoint = Collections.<ScheduledStop> binarySearch(this.scheduledStops, newStop);
      this.scheduledStops.add(insertionPoint, newStop);

      this.nextTravelGraph.recordDropRequest(destinationFloor);
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
      final ScheduledStop newStop;
      if (direction == DirectionOfTravel.GOING_UP) {
         newStop = new ScheduledAscendingPickUp(floorIndex);
      } else if (direction == DirectionOfTravel.GOING_DOWN) {
         newStop = new ScheduledDescendingPickUp(floorIndex);
      } else if (direction == DirectionOfTravel.STOPPED) {
         throw new IllegalArgumentException();
      } else {
         throw new NullPointerException();
      }
      
      // Pickup assignments can preempt the current destination if they are "on the way" to it.
      
      boolean changedDestination = true;
      if (this.currentDrop != null) {
         if (newStop.compareTo(this.currentDrop) < 0) {
            // The new pickup is in our direction of travel and we'll reach it before the current drop.  Re-queue the current drop and change destination.
            final int insertionPoint = Collections.<ScheduledStop> binarySearch(this.scheduledStops, this.currentDrop);
            log.info(String.format("Re-queuing previous stop at index %d should be 0.", insertionPoint));
            this.scheduledStops.add(insertionPoint, this.currentDrop);
            if (this.currentPickup != null) {
               this.scheduledStops.add(insertionPoint + 1, this.currentPickup);
            }
            this.currentDrop = null;
            this.currentPickup = newStop;
         } else if((this.currentDrop.getFloorIndex() == floorIndex) && (this.currentDirection == direction)) {
            // We just picked up a pickup request for the same floor as the current drop and in the same direction we're already travelling, so we
            // can just add it to the current destination as current pickup.
            Preconditions.checkState(this.currentPickup == null, "We should not be handling a pickup already assigned as next!");
            this.currentPickup = newStop;
            changedDestination = false;
         } else {
            // We picked up a pickup request that is after our current destination.  Queue it.
            final int insertionPoint = Collections.<ScheduledStop> binarySearch(this.scheduledStops, newStop);
            log.info(String.format("Queuing new pickup at index %d.", insertionPoint));
            this.scheduledStops.add(insertionPoint, newStop);
            changedDestination = false;
         }
      } else if (this.currentPickup != null) {
         if (newStop.compareTo(this.currentPickup) < 0) {
            // The new pickup is in our direction of travel and we'll reach it before the current drop.  Re-queue the current drop and change destination.
            final int insertionPoint = Collections.<ScheduledStop> binarySearch(this.scheduledStops, this.currentPickup);
            log.info(String.format("Re-queuing previous stop at index %d should be 0.", insertionPoint));
            this.scheduledStops.add(insertionPoint, this.currentPickup);
            this.currentPickup = newStop;
         } else {
            // We picked up a pickup request that is after our current destination.  Queue it.
            final int insertionPoint = Collections.<ScheduledStop> binarySearch(this.scheduledStops, newStop);
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
      final ScheduledStop oldStop;
      if (direction == DirectionOfTravel.GOING_UP) {
         oldStop = new ScheduledDescendingPickUp(floorIndex);
      } else if (direction == DirectionOfTravel.GOING_DOWN) {
         oldStop = new ScheduledDescendingPickUp(floorIndex);
      } else if (direction == DirectionOfTravel.STOPPED) {
         throw new IllegalArgumentException();
      } else {
         throw new NullPointerException();
      }

      boolean changedDestination = true;
      if ((this.currentPickup != null) &&
         (this.currentPickup.getFloorIndex() == floorIndex) &&
         (this.currentPickup.getOutbound() == direction)
      ) {
            this.currentPickup = null;
            if (this.currentDrop != null) {
               changedDestination = false;
            } else {
               this.popNextDestination();
            }
      } else {
         final int insertionPoint = Collections.<ScheduledStop> binarySearch(this.scheduledStops, oldStop);
         final ScheduledStop match = this.scheduledStops.get(insertionPoint);
         Preconditions.checkState(match.compareTo(oldStop) == 0);
         this.scheduledStops.remove(insertionPoint);
         changedDestination = false;
      }

      this.nextTravelGraph.recordCancelledPickup(floorIndex, direction);
      return changedDestination;
   }
   
   
   @Override
   public boolean trackSlowingThrough(int floorIndex, DirectionOfTravel direction) {
      if ((this.currentDirection == direction) && (this.getCurrentDestination() == floorIndex)) {
         this.currentFloor = floorIndex;
         return true;
      }
      
      return false;
   }

   
   @Override
   public boolean trackTravelThrough(int floorIndex, DirectionOfTravel direction) {
      if ((this.currentDirection == direction) &&
         (((this.getCurrentDestination()> floorIndex) && (direction == DirectionOfTravel.GOING_UP)) ||
            ((this.getCurrentDestination() < floorIndex) && (direction == DirectionOfTravel.GOING_DOWN)))) {
         this.currentFloor = floorIndex;
         return true;
      }
      
      return false;
   }

   
   private void popNextDestination() {
      if (this.scheduledStops.size() > 1) {
         ScheduledStop nextStop = this.scheduledStops.remove(0);
         if (nextStop.isDropOff()) {
            this.currentDrop = (ScheduledDropOff) nextStop;
            nextStop = this.scheduledStops.get(0);
            if (nextStop.isPickup()) {
               ScheduledPickUp nextPickup = (ScheduledPickUp) nextStop;
               if (nextPickup.getFloorIndex() == this.currentDrop.getFloorIndex()) {
                  this.currentPickup = nextPickup;
               }
            }
         } else if (nextStop.isPickup()) {
            this.currentPickup = (ScheduledPickUp) nextStop;
            
         }

         ScheduledStop 
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


   enum StopPurpose
   {
      DROP_OFF,
      UPWARD_CALL,
      DOWNWARD_CALL,
      REVERSE;
   }


   abstract class ScheduledStop
   implements Comparable<ScheduledStop>
   {
      final int floorIndex;
      final StopPurpose purpose;


      ScheduledStop( int floorIndex, StopPurpose purpose )
      {
         Preconditions.checkArgument(0 <= floorIndex && floorIndex < PassengerManifest.this.numFloors);
         Preconditions.checkNotNull(purpose);

         this.floorIndex = floorIndex;
         this.purpose = purpose;
      }


      public StopPurpose getPurpose() {
         return this.purpose;
      }

      abstract boolean isReversal();
      
      abstract boolean isPickup();
      
      abstract boolean isDropOff();

      abstract int breakComparisonTie(ScheduledStop otherStop);


      PassengerManifest manifest()
      {
         return PassengerManifest.this;
      }


      int getFloorIndex() {
         return this.floorIndex;
      }
      
      
      protected abstract DirectionOfTravel getOutbound();


      @Override
      public int compareTo(ScheduledStop o2)
      {
         Preconditions.checkNotNull(o2);
         Preconditions.checkArgument(PassengerManifest.this == o2.manifest());

         if (this == o2) { return 0; }

         // If the main direction is GOING_DOWN, use the negative for all floor indices. This inversion transforms all
         // floor-to-floor
         // comparison semantics s uch that they become identical to the GOING_UP semantics with positive floor values.
         // This get rid of
         // a whole lot of redundant branching!
         final DirectionOfTravel o1Outbound = this.getOutbound();
         final DirectionOfTravel o2Outbound = this.getOutbound();
         final DirectionOfTravel originOutbound = PassengerManifest.this.currentDirection;
         final int originFloor, o1Floor, o2Floor;
         if (originOutbound == DirectionOfTravel.GOING_DOWN) {
            originFloor = PassengerManifest.this.currentFloor * -1;
            o1Floor = this.floorIndex * -1;
            o2Floor = o2.floorIndex * -1;
         } else {
            originFloor = PassengerManifest.this.currentFloor;
            o1Floor = this.floorIndex;
            o2Floor = o2.floorIndex;
         }

         if (o1Outbound == o2Outbound) {
            if (o1Floor == o2Floor) {
               // Both depart the same floor in the same direction, therefore they are scheduled together.
               return this.breakComparisonTie(o2);
            } else if (o1Outbound == originOutbound) {
               // Both are departing the same direction the car is currently travelling.  To compare the floor indices, it is necessary to
               // know whether one, both, or neither is above the origin floor.
               if (o1Floor > o2Floor) {
                  if (o2Floor >= originFloor) {
                     return 1;
                  } else if (o1Floor >= originFloor) {
                     return -1;
                  } else {
                     return 1;
                  }
               } else if (o1Floor < o2Floor) {
                  if (o1Floor >= originFloor) {
                     return -1;
                  } else if (o2Floor >= originFloor) {
                     return 1;
                  } else {
                     return -1;
                  }
               }
            } else if (o1Floor > o2Floor) {
               return -1;
            } else {
               return 1;
            }
         } else if (o1Outbound == originOutbound) {
            if (o1Floor >= originFloor) {
               return -1;
            } else {
               return 1;
            }
         } else if (o2Floor >= originFloor) {
            return 1;
         }

         return -1;
      }
   }

  
   abstract class ScheduledPickUp extends ScheduledStop {
      ScheduledPickUp( int floorIndex, DirectionOfTravel direction )
      {
         super(floorIndex, direction == DirectionOfTravel.GOING_UP ? StopPurpose.UPWARD_CALL : StopPurpose.DOWNWARD_CALL);
      }

      boolean isReversal()
      {
         return false;
      }

      @Override
      boolean isPickup()
      {
         return true;
      }

      @Override
      boolean isDropOff()
      {
         return false;
      }

   }
   

   class ScheduledAscendingPickUp
   extends ScheduledPickUp
   {
      ScheduledAscendingPickUp( int floorIndex )
      {
         super(floorIndex, DirectionOfTravel.GOING_UP);
         Preconditions.checkArgument(floorIndex < (PassengerManifest.this.numFloors - 1));
         Preconditions.checkArgument(floorIndex >= 0);
      }

      @Override
      protected DirectionOfTravel getOutbound()
      {
         return DirectionOfTravel.GOING_UP;
      }
      
      
      @Override
      protected int breakComparisonTie( ScheduledStop otherStop ) {
         if (PassengerManifest.this.getCurrentDirection() == DirectionOfTravel.GOING_DOWN) {
            return 1;
         } else if (otherStop.isDropOff()) {
            return 1;
         } else if (otherStop.getPurpose() == StopPurpose.UPWARD_CALL) {
            return 0;
         }

         return -1;
      }
   }


   class ScheduledDescendingPickUp
   extends ScheduledPickUp
   {
      ScheduledDescendingPickUp( int floorIndex )
      {
         super(floorIndex, DirectionOfTravel.GOING_DOWN);
         Preconditions.checkArgument(floorIndex < PassengerManifest.this.numFloors);
         Preconditions.checkArgument(floorIndex > 0);
      }

      @Override
      protected DirectionOfTravel getOutbound()
      {
         return DirectionOfTravel.GOING_DOWN;
      }
      
      @Override
      protected int breakComparisonTie( ScheduledStop otherStop ) {
         if (PassengerManifest.this.getCurrentDirection() == DirectionOfTravel.GOING_UP) {
            return 1;
         } else if (otherStop.isDropOff()) {
            return 1;
         } else if (otherStop.getPurpose() == StopPurpose.DOWNWARD_CALL) {
            return 0;
         }

         return -1;
      }
   }


   class ScheduledDropOff
   extends ScheduledStop
   {
      ScheduledDropOff( int floorIndex )
      {
         super(floorIndex, StopPurpose.DROP_OFF);
         if (PassengerManifest.this.currentDirection == DirectionOfTravel.GOING_UP) {
            Preconditions.checkArgument(floorIndex > PassengerManifest.this.currentFloor);
         } else if (PassengerManifest.this.currentDirection == DirectionOfTravel.GOING_UP) {
            Preconditions.checkArgument(floorIndex < PassengerManifest.this.currentFloor);
         }
      }


      boolean isReversal()
      {
         return false;
      }


      @Override
      boolean isPickup()
      {
         return false;
      }


      @Override
      boolean isDropOff()
      {
         return true;
      }


      @Override
      protected DirectionOfTravel getOutbound()
      {
         return PassengerManifest.this.currentDirection;
      }
      
      
      @Override
      protected int breakComparisonTie( ScheduledStop otherStop ) {
         if (otherStop.isDropOff()) {
            return 0;
         }

         return -1;
      }
   }


   class ScheduledReversal
   extends ScheduledStop
   {
      ScheduledReversal( int floorIndex )
      {
         super(floorIndex, StopPurpose.REVERSE);
         Preconditions
            .checkArgument((floorIndex == 0) || (floorIndex == (PassengerManifest.this.numFloors - 1)));
      }


      boolean isReversal()
      {
         return true;
      }


      @Override
      boolean isPickup()
      {
         return false;
      }


      @Override
      boolean isDropOff()
      {
         return false;
      }


      @Override
      protected DirectionOfTravel getOutbound()
      {
         if (this.floorIndex == 0) { return DirectionOfTravel.GOING_UP; }

         return DirectionOfTravel.GOING_DOWN;
      }
      
      
      @Override
      protected int breakComparisonTie( ScheduledStop otherStop ) {
         if (otherStop.isReversal()) {
            return 0;
         } else if (otherStop.getOutbound() == this.getOutbound()) {
            return 1;
         }

         return -1;
      }
   }
}