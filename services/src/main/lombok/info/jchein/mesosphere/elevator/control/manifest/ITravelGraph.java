package info.jchein.mesosphere.elevator.control.manifest;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

public interface ITravelGraph
{
//   void prepareForBoarding(int floorIndex);

//   void prepareForDisembarking(int floorIndex);

   double getCurrentWeightLoad();

   void recordDisembarked(double outgoingLoad);

   void recordBoarded(double incomingLoad);

   void recordDropRequest(int dropOffFloorIndex);

   void recordAssignedPickup(int floorIndex, DirectionOfTravel direction);

   void recordCancelledPickup(int floorIndex, DirectionOfTravel direction);

   void recordDoorsOpening(int floorIndex);
   
   void recordDoorsClosed();
}
