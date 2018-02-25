package info.jchein.mesosphere.elevator.control.sdk;

import info.jchein.mesosphere.elevator.common.bootstrap.InitialCarState;

public interface IElevatorCarPort {
//	Subscription bootstrapDriver(Observer<StopItineraryUpdated> observer, double floorHeight, double weightLoad, BitSet dropOffRequests);
   void bootstrapDriver(InitialCarState initialData);
	
	void dropOffRequested(int floorIndex);
		
	void updateWeightLoad(double previousLoad, double weightDelta, double currentLoad);

	void parkedAtLanding(long timeDelta);
	
	void passengerDoorsOpened(long timeDelta);

	void passengerDoorsClosed(long timeDelta);

   int getCarIndex();
   
   int getCurrentFloorIndex();

   double getExpectedLocation();

   double getCurrentWeightLoad();

   double getMaximumWeightLoad();

//	void updateLocation(double floorHeight, boolean brakingToStop);
//	void openingAccessDoor();
//	void readyForDeparture();
//	void travelledThroughFloor();
//	void doorStateChanging(DoorState newStatus);
//   ElevatorCarSnapshot getSnapshot();
//	void travelledThroughFloor(int floorIndex, DirectionOfTravel direction);
}
