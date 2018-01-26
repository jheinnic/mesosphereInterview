package info.jchein.mesosphere.elevator.domain.sdk;

import java.util.BitSet;

import info.jchein.mesosphere.elevator.domain.common.ElevatorCarSnapshot;
import info.jchein.mesosphere.elevator.domain.dispatch.event.StopItineraryUpdated;
import rx.Observer;
import rx.Subscription;

public interface IElevatorCarPort {
//	Subscription bootstrapDriver(Observer<StopItineraryUpdated> observer, double floorHeight, double weightLoad, BitSet dropOffRequests);
	
	void dropOffRequested(int floorIndex);

//	void doorStateChanging(DoorState newStatus);

	void updateLocation(double floorHeight, boolean brakingToStop);
	
	void updateWeightLoad(double weightLoad);

	void landedAtFloor(int floorIndex);

	void readyForDeparture();

//   ElevatorCarSnapshot getSnapshot();

//	void travelledThroughFloor(int floorIndex, DirectionOfTravel direction);

//	void slowedForArrival(int floorIndex);
}
