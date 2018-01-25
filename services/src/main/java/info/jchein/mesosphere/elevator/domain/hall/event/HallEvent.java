package info.jchein.mesosphere.elevator.domain.hall.event;

public interface HallEvent {
	public HallEventType getEventType();
    public long getTimeIndex();
    public int getFloorIndex();
}
