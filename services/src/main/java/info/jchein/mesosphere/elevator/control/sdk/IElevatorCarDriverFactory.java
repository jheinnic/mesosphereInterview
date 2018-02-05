package info.jchein.mesosphere.elevator.control.sdk;

import info.jchein.mesosphere.elevator.common.InitialElevatorCarState;

public interface IElevatorCarDriverFactory
{
   IElevatorCarDriver allocateDriver( IElevatorCarPort port );
}
