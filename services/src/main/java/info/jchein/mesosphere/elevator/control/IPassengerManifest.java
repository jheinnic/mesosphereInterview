package info.jchein.mesosphere.elevator.control;

import java.util.BitSet;

import org.jgrapht.alg.util.Pair;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.control.event.DropOffRequested;
import info.jchein.mesosphere.elevator.control.event.WeightLoadUpdated;

public interface IPassengerManifest
{
   int getCurrentFloor();

   int getCurrentDestination();

   DirectionOfTravel getCurrentDirection();

   DirectionOfTravel getNextDirection();

   boolean hasCurrentFloorStopRequest();
   
   BitSet getFloorStops();
   
   double getCurrentWeightLoad();
   
   PerformanceEstimate estimatePerformance();
   
   PickupImpactEstimate estimatePickupImpact(int floorIndex, DirectionOfTravel direction, ImmutableList<Pair<Integer, Double>> passengers);

   void trackDropRequest(int floorIndex);

   boolean trackAssignedPickup(int floorIndex, DirectionOfTravel direction);

   boolean trackCanceledPickup(int floorIndex, DirectionOfTravel direction);

   boolean trackTravelThrough(int floorIndex, DirectionOfTravel direction);

   boolean trackSlowingThrough(int floorIndex, DirectionOfTravel direction);

   void trackWeightChange(double previousWeight, double weightDelta, double currentWeight);

   void trackDoorsOpening();

   void trackDoorsClosed();
}
