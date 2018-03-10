package info.jchein.mesosphere.elevator.control.manifest;


import java.util.BitSet;

import org.statefulj.fsm.model.StateActionPair;
import org.statefulj.persistence.annotations.State;
import org.statefulj.persistence.annotations.State.AccessorType;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import lombok.Data;


@Data
public class FloorOfOrigin
{
   @State(accessorType = AccessorType.METHOD, getMethodName = "getState", setMethodName = "setState")
   private String state;

   private final int pickupFloor;
   private final DirectionOfTravel direction;
   private long pickupTime;
   private BitSet possibleDestinations;
   private BitSet stopsSincePickup;
   private double ownWeightAtPickup;
   private double ownWeightRemaining;
   private double peerWeightOnboard;
   private double peerWeightDisembarked;
   private boolean finalStop;


   FloorOfOrigin( int pickupFloor, DirectionOfTravel direction )
   {
      this.pickupFloor = pickupFloor;
      this.direction = direction;
      this.possibleDestinations = new BitSet();
      this.stopsSincePickup = new BitSet();
   }


   void beginPickup(long pickupTime)
   {
      this.pickupTime = pickupTime;
      this.peerWeightOnboard -= this.peerWeightDisembarked;
      this.peerWeightDisembarked = 0;
   }

   
   void trackDropRequest(int floorIndex)
   {
      if ((this.direction == DirectionOfTravel.GOING_UP && floorIndex > this.pickupFloor) ||
         (this.direction == DirectionOfTravel.GOING_DOWN && floorIndex < this.pickupFloor))
      {
         this.possibleDestinations.set(floorIndex);
      }
   }


   boolean trackCurrentStop(int floorIndex)
   {
      boolean retVal = false;
      if (this.possibleDestinations.get(floorIndex)) {
         this.stopsSincePickup.nextSetBit(floorIndex);
         this.finalStop = this.stopsSincePickup.size() == this.possibleDestinations.size();
         retVal = true;
      }

      return retVal;
   }

   boolean isPotentialStop(int floorIndex)
   {
      return this.possibleDestinations.get(floorIndex);
   }

   boolean isLastStop(int floorIndex)
   {
      return this.possibleDestinations.get(floorIndex) &&
         (this.stopsSincePickup.size() == this.possibleDestinations.size());
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
      this.ownWeightAtPickup += weightLoad;
   }


   void trackPeerBoardingWeight(double weightLoad)
   {
      this.peerWeightOnboard += weightLoad;
   }


   double getOwnWeightRemaining() 
   {
      return this.ownWeightRemaining;
   }


   public StateActionPair<FloorOfOrigin> onDoorsOpened(String event, Object... args)
   {
      return null;
   }


   public int getFloorIndex()
   {
      return this.pickupFloor;
   }
}
