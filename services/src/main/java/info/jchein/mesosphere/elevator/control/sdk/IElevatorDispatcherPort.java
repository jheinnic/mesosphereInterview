package info.jchein.mesosphere.elevator.control.sdk;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

/**
 * 
 * @author jheinnic
 */
public interface IElevatorDispatcherPort {
	public void assignPickupCar(int floorIndex, DirectionOfTravel initialDirection, int carIndex);
}
