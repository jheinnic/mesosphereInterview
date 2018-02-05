package info.jchein.mesosphere.elevator.control.event;

import info.jchein.mesosphere.elevator.common.Event;

public interface ElevatorCarEvent extends Event {
    public int getCarIndex();
    public long getCarSequence();
}
