package info.jchein.mesosphere.elevator.domain.car.event;

public interface CarEvent {
	CarEventType getEventType();

	int getCarIndex();

	long getTimeIndex();
}
