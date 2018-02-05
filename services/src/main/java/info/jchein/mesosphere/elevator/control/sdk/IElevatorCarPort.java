package info.jchein.mesosphere.elevator.control.sdk;

public interface IElevatorCarPort {
//	Subscription bootstrapDriver(Observer<StopItineraryUpdated> observer, double floorHeight, double weightLoad, BitSet dropOffRequests);
	
	void dropOffRequested(int floorIndex);
		
	void updateWeightLoad(double weightLoad);

	void slowedForArrival();

	void parkedAtLanding();
	
	void passengerDoorsClosed();

//	void updateLocation(double floorHeight, boolean brakingToStop);
//	void openingAccessDoor();
//	void readyForDeparture();
//	void travelledThroughFloor();
//	void doorStateChanging(DoorState newStatus);
//   ElevatorCarSnapshot getSnapshot();
//	void travelledThroughFloor(int floorIndex, DirectionOfTravel direction);
}
