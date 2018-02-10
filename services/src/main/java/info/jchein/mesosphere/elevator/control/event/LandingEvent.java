package info.jchein.mesosphere.elevator.control.event;

public interface LandingEvent extends Event {
    public int getFloorIndex();
}
