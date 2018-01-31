package info.jchein.mesosphere.elevator.domain.event;

public interface ElevatorCarEvent extends Event {
    public int getCarIndex();
    public long getCarSequence();
}
