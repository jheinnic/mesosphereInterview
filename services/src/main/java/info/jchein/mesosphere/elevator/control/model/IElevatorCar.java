package info.jchein.mesosphere.elevator.control.model;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

/**
 * The inward facing interface the software adapter for an elevator car provides for the elevator group control's
 * use.  It very closely resembles the Driver API because most such operations are delegated to the embedded hardware
 * implementation.
 * 
 * @author jheinnic
 */
interface IElevatorCar {
	// void onFloorSensorTriggered(int floorIndex, DirectionOfTravel direction);
   void confirmNextDispatch(int floorIndex);
	void acceptPickupRequest(int floorIndex, DirectionOfTravel direction);
	void cancelPickupRequest(int floorIndex, DirectionOfTravel direction);
}
