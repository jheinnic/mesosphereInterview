package info.jchein.mesosphere.elevator.domain.sdk;

import java.util.BitSet;

import info.jchein.mesosphere.elevator.domain.common.ElevatorCarSnapshot;
import info.jchein.mesosphere.elevator.domain.common.SpeedOfTravel;
import info.jchein.mesosphere.elevator.domain.dispatch.event.StopItineraryUpdated;
import rx.Observer;
import rx.Subscription;

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
