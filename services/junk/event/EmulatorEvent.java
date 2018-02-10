package info.jchein.mesosphere.elevator.emulator.event;

public interface EmulatorEvent
{
//   EmulatorEventType getEventType();
   long getClockTime();
   long getSequenceId();
}
