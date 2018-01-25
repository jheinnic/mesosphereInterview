package info.jchein.mesosphere.elevator.domain.sdk;

public interface IElevatorCarPort {
	void dropOffRequested(int floorIndex);

//	void doorStateChanging(DoorState newStatus);

	void parkedForBoarding(int floorIndex, double weightOnArrival);

	void readyForDeparture(int floorIndex, double weightOnDeparture);

//	void travelledThroughFloor(int floorIndex, DirectionOfTravel direction);

//	void slowedForArrival(int floorIndex);
}
