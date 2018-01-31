package info.jchein.mesosphere.elevator.domain.event;

public interface LandingEvent extends Event {
    public int getFloorIndex();
    public long getFloorSequence();
}
