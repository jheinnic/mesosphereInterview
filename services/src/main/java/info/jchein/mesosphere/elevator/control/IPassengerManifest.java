package info.jchein.mesosphere.elevator.control;

import org.jgrapht.alg.util.Pair;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

public interface IPassengerManifest
{
   double getCurrentWeightLoad();
   
   PerformanceEstimate estimatePerformance();
   
   PickupImpactEstimate estimatePickupImpact(int floorIndex, DirectionOfTravel direction, ImmutableList<Pair<Integer, Double>> passengers);

   void trackAssignedPickup(int floorIndex, DirectionOfTravel direction);

   void trackCanceledPickup(int floorIndex, DirectionOfTravel direction);
}
