package info.jchein.mesosphere.elevator.domain.car.event;

public interface ElevatorCarEvent {
    public long getClockTime();
    
    public int getCarIndex();
}
