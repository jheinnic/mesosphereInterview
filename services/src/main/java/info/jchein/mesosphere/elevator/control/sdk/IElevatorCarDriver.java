package info.jchein.mesosphere.elevator.control.sdk;

import info.jchein.mesosphere.elevator.common.InitialElevatorCarState;

public interface IElevatorCarDriver
{
   public InitialElevatorCarState initialize();
   public void travelTo(int floorIndex);
   public void openDoors();
}
