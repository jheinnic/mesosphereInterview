package info.jchein.mesosphere.elevator.domain.sdk;

import info.jchein.mesosphere.elevator.domain.common.InitialElevatorCarState;

public interface IElevatorCarDriverFactory<T extends IElevatorCarDriver>
{
   T initialize( IElevatorCarPort port, InitialElevatorCarState data );
}
