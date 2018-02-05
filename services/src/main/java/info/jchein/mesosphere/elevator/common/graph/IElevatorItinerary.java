package info.jchein.mesosphere.elevator.common.graph;


import java.util.BitSet;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.evaluation.PredictedOutcome;


public interface IElevatorItinerary
{
   CarPickupHeading getNextDestination();

   IElevatorItinerary withBoardingStopCompleted(long callTime, long pickupTime, long departureTime, int carIndex, int floorIndex,
      DirectionOfTravel nextHeading, double originalWeight, double minWeight, double maxWeight, double finalWeight, BitSet dropRequests);

   IElevatorItinerary withPickupRequest(long callTime, int floorIndex, DirectionOfTravel nextHeading);

   IElevatorItinerary withDropOffRequest(long callTime, int carIndex, int floorIndex);

   IElevatorItinerary withTravelThroughFloor(long clockTime, int carIndex, int floorIndex, DirectionOfTravel direction);
   
   IElevatorItinerary withBoardingStopInitiated(long clockTime, int carIndex, int floorIndex);
   
   IElevatorItinerary withDispatchedTo(long clockTime, int carIndex, int floorIndex, DirectionOfTravel direction, boolean forPickup, boolean forDropOff);

   PredictedOutcome predictOutcome();
}
/*
18004663337
10658
10658461
*/