package info.jchein.mesosphere.elevator.control.sdk;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.bootstrap.InitialCarState;

public interface IElevatorCarDriver
{
   public static final String CAR_DRIVER_BEAN_NAME = "elevatorCarDriver";

   public InitialCarState initialize();

   public void dispatchTo(int floorIndex);
   public void openDoors(DirectionOfTravel direction);
}
