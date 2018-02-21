package info.jchein.mesosphere.elevator.control.sdk;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

public interface IElevatorLandingsPort
{
   void callForPickup(int floorIndex, DirectionOfTravel direction);
   void triggerFloorSensor(int carIndex, int floorIndex, DirectionOfTravel direction);
}
