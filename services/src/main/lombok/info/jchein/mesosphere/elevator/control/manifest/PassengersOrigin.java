package info.jchein.mesosphere.elevator.control.manifest;

import java.util.BitSet;

import lombok.Data;

@Data
//@Builder(toBuilder=true)
public class PassengersOrigin
{
   private int pickupFloor;
   private long pickupTime;
   private BitSet originalDestinations;
   private BitSet stopsSincePickup;
   private double ownWeightAtPickup;
   private double peerWeightOnboard;
   private double peerWeightDisembarked;
   private double ownWeightDisembarked;
   private boolean finalStop;


   PassengersOrigin( int pickupFloor, long pickupTime, BitSet originalDestinations,
      double ownWeightAtPickup, double otherWeightAtPickup )
   {
      this.pickupFloor = pickupFloor;
      this.pickupTime = pickupTime;
      this.originalDestinations = (BitSet) originalDestinations.clone();
      this.stopsSincePickup = new BitSet();
      this.ownWeightAtPickup = ownWeightAtPickup;
      this.peerWeightOnboard = otherWeightAtPickup;
      this.finalStop = false;
   }


   boolean trackCurrentStop(int floorIndex)
   {
      boolean retVal = false;
      if (this.originalDestinations.get(floorIndex)) {
         this.stopsSincePickup.nextSetBit(floorIndex);
         this.finalStop = this.stopsSincePickup.size() == this.originalDestinations.size();
         retVal = true;
      }

      return retVal;
   }
   
   boolean isLastStop(int floorIndex) {
      return this.originalDestinations.get(floorIndex) && (this.stopsSincePickup.size() == this.originalDestinations.size());
   }
   
   void trackDisembarkingWeight(double weightLoad) {
      final double weightRemaining = (this.ownWeightAtPickup - this.ownWeightDisembarked);
      if (this.finalStop) {
         if (weightLoad < weightRemaining) {
            throw new IllegalArgumentException(
               String.format("Insufficient disembark weight (%f) for actual weight remaining (%f)", weightLoad, weightRemaining));
         }
         this.doDisembark(weightRemaining);
      } else {
         this.peerWeightDisembarked += weightLoad;
         if (this.peerWeightDisembarked > this.peerWeightOnboard) {
            final double ownDisembarkedWeight = this.peerWeightDisembarked - this.peerWeightOnboard;
            if (ownDisembarkedWeight > weightRemaining) {
               this.peerWeightDisembarked -= weightRemaining;
               throw new IllegalArgumentException(
                  String.format("Insufficient weight remaining (%f) for actual disembarking weight with no residual peer weight (%f)",
                     weightRemaining, ownDisembarkedWeight));
            }
            this.peerWeightDisembarked = this.peerWeightOnboard;
            this.doDisembark(ownDisembarkedWeight);
         }
      }
   }
   
   void trackBoardingWeight(double weightLoad) {
      this.peerWeightOnboard += weightLoad;
   }

   private void doDisembark(double weightRemaining)
   {
      // TODO Auto-generated method stub
      
   }
}
