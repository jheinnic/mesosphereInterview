package info.jchein.mesosphere.elevator.control.event;

import info.jchein.mesosphere.elevator.common.Event;

public interface LandingEvent extends Event {
    public int getFloorIndex();
    public long getFloorSequence();
}
