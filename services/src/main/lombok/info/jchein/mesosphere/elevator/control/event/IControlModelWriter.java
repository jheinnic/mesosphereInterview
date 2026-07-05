package info.jchein.mesosphere.elevator.control.event;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

public interface IControlModelWriter
{
   void onAvailableCarSignalOn(int floorIndex, DirectionOfTravel direction);

   void onAvailableCarSignalOff(int floorIndex, DirectionOfTravel direction);

   void onDepartedLanding(int carIndex, int floorIndex, DirectionOfTravel direction);

   void onFloorSensorTriggered(int carIndex, int floorIndex, DirectionOfTravel direction);
   
   void onLocationEstimateUpdated(int carIndex);
   
   void onParkedAtLanding(int carIndex, int floorIndex, DirectionOfTravel direction);
   
   void onPassengerDoorsClosed(int carIndex);
   
   void onPassengerDoorsOpened(int carIndex);
   
   void onPickupCallAdded(int floorIndex, DirectionOfTravel direction);
   
   void onPickupCallRemoved(int floorIndex, DirectionOfTravel direction);
   
   void onSlowedForArrival(int carIndex, int floorIndex, DirectionOfTravel direction);
   
   void onTravelledPastFloor(int carIndex, int floorIndex, DirectionOfTravel direction);
   
   void onWeightLoadUpdated(int carIndex, double increases, double decreases, double absolute);
   
//   void transformSchedulerOutput(Transform)
}
