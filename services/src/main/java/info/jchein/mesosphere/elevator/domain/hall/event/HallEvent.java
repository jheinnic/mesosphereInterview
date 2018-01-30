package info.jchein.mesosphere.elevator.domain.hall.event;

public interface HallEvent {
    public long getClockTime();
    public int getFloorIndex();
}
