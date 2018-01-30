package info.jchein.mesosphere.elevator.domain.sdk;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;

/**
 * 
 * @author jheinnic
 */
public interface IElevatorDispatcherPort {
	public void assignPickupCar(int floorIndex, DirectionOfTravel initialDirection, int carIndex);
}
