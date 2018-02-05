package info.jchein.mesosphere.elevator.emulator.event;

public interface EmulatedPassengerEvent extends EmulatorEvent
{
   String getPassengerId();
}
