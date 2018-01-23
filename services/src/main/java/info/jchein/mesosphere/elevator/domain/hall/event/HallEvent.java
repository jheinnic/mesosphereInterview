package info.jchein.mesosphere.elevator.domain.hall.event;

import info.jchein.mesosphere.domain.event.IEvent;
import info.jchein.mesosphere.elevator.domain.hall.event.HallEventType;

public interface HallEvent extends IEvent<HallEventType> {
    public long getTimeIndex();
    public int getFloorIndex();
}
