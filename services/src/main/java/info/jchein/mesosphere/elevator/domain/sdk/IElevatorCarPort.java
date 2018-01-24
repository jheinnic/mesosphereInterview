package info.jchein.mesosphere.elevator.domain.sdk;

import info.jchein.mesosphere.elevator.domain.car.event.DropOffRequested;
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.domain.common.DoorState;

public interface IElevatorCarPort {
	void dropOffRequsted(int floorIndex);

	void destinationAccepted(int floorIndex, DirectionOfTravel direction);

	void destinationRejected(int rejectedFloorIndec, int retainedFloorIndex, DirectionOfTravel direction, String reason);

	void doorStateChanging(DoorState newStatus);

	void parkedForBoarding(int floorIndex, double weightOnArrival);

	void idleForDeparture(int floorIndex, double weightOnDeparture);

	void travelledThroughFloor(int floorIndex, DirectionOfTravel direction);

	void slowedForArrival(int floorIndex);

//	void passengerLoadUpdated(
//		int estimatedArrivals, int estimatedDepartures, int estimatedPassengerCount,
//		double weightOnArrival, double weightOnDeparture);

}
