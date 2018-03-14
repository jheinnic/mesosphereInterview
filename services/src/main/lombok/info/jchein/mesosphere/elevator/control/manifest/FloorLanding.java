package info.jchein.mesosphere.elevator.control.manifest;

import java.util.BitSet;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.statefulj.persistence.annotations.State;
import org.statefulj.persistence.annotations.State.AccessorType;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import lombok.Data;

@Data
class FloorLanding implements ITravelQueueNode
{
   @State(accessorType = AccessorType.METHOD, getMethodName = "getState", setMethodName = "setState")
   private String state;

   private final int pickupFloor;
   private long pickupTime;
   private DirectionOfTravel direction;
   private BitSet priorDropFloors;
   private BitSet ownDropFloors;
//   private BitSet lateDropFloors;
   private int pendingFloorCount;
   private double ownWeightOnPickup;
   private double ownWeightOnVisit;
   private double ownWeightRemaining;
   private double peerWeightObserved;
   private double peerWeightDeparted;


   FloorLanding( int pickupFloor )
   {
      this.pickupFloor = pickupFloor;
      this.pickupTime = -1;
      this.direction = null;
      this.priorDropFloors = new BitSet();
      this.ownDropFloors = new BitSet();
//      this.lateDropFloors = new BitSet();
      this.pendingFloorCount = -1;
      this.ownWeightOnPickup = -1;
      this.ownWeightOnVisit = -1;
      this.ownWeightRemaining = -1;
      this.peerWeightObserved = 0;
      this.peerWeightDeparted = 0;
   }


   public int getFloorIndex()
   {
      return this.pickupFloor;
   }
   
   
   public long getPickupTime()
   {
      return this.pickupTime;
   }


   double getOwnWeightRemaining() 
   {
      return this.ownWeightRemaining;
   }


   void trackBeginPickup(long pickupTime)
   {
      this.pickupTime = pickupTime;
      this.activePickup = true;
   }
   

   void trackCompletePickup() 
   {
      this.peerWeightOnboard -= this.peerWeightDisembarked;
      this.peerWeightDisembarked = 0;
      this.unvisittedDropFloors.or(this.possibleDropFloors);
      this.activePickup = false;
   }
   
   void trackBeginVisit(long pickupTime)
   {
      this.unvisittedDropFloors.clear(TravelGraph.this.currentFloorStop);
      this.ownWeightOnVisit = this.ownWeightRemaining;
   }
   
   void trackCompleteVisit()
   {
      final int fromFloorIndex = this.pickupFloor;
      final int toFloorIndex = TravelGraph.this.currentFloorStop;
   }


   void trackDropRequest(int floorIndex)
   {
      if (this.direction.floorOrder(this.pickupFloor, floorIndex) < 0) {
         this.possibleDropFloors.set(floorIndex);
         if (this.activePickup) {
            this.
         }
      }
   }


   boolean isPotentialStop()
   {
      return this.possibleDropFloors.get(TravelGraph.this.currentFloorStop);
   }

   boolean isLastStop()
   {
      return this.isPotentialStop() && (this.unvisittedDropFloors.size() == 0);
   }


   void trackDisembarkingWeight(double weightLoad)
   {
      this.peerWeightDisembarked += weightLoad;
      if (this.peerWeightDisembarked > this.peerWeightOnboard) {
         final double ownDisembarkedWeight = this.peerWeightDisembarked - this.peerWeightOnboard;
         if (ownDisembarkedWeight > this.ownWeightRemaining) { throw new IllegalArgumentException(
            String.format(
               "Insufficient weight remaining (%f) for actual disembarking weight with no residual peer weight (%f)",
               this.ownWeightRemaining,
               ownDisembarkedWeight)); }
         this.peerWeightDisembarked = this.peerWeightOnboard;
         this.ownWeightRemaining -= ownDisembarkedWeight;
      }
   }


   void trackOwnBoardingWeight(double weightLoad)
   {
      this.ownWeightOnPickup += weightLoad;
   }


   void trackPeerBoardingWeight(double weightLoad)
   {
      this.peerWeightOnboard += weightLoad;
   }
}