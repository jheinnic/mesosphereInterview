package info.jchein.mesosphere.elevator.control.sdk;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.physics.JourneyArc;

public interface IElevatorCarDriver
{
   public static final String CAR_DRIVER_BEAN_NAME = "elevatorCarDriver";

   public JourneyArc dispatchTo(int floorIndex);
   public void slowForArrival(); 
   public void openDoors(DirectionOfTravel direction);
   public void closeDoors();
}
