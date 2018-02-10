package info.jchein.mesosphere.elevator.control.sdk;

public interface IElevatorCarDriverFactory
{
   IElevatorCarDriver allocateDriver( IElevatorCarPort port );
}
