package info.jchein.mesosphere.elevator.emulator.event;

public interface EmulatorEvent
{
   long getClockTime();
   EmulatorEventType getEventType();
}
