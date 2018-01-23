package info.jchein.mesosphere.elevator.domain.sdk;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.domain.hall.event.FloorSensorTriggered;

public interface IHallPanelPort {
	int getFloorIndex();
	void requestPickup(DirectionOfTravel direction);
	void cancelPickupRequest(DirectionOfTravel direction);
	void floorSensorTriggered(int carIndex, double floorHeight, DirectionOfTravel direction);
}
