package info.jchein.mesosphere.elevator.control.sdk;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.bootstrap.InitialCarState;

public interface IElevatorCarDriver
{
   public InitialCarState initialize();

   public void dispatchCar(int carIndex, int floorIndex, DirectionOfTravel direction);
   public void openDoors(int carIndex);
}
