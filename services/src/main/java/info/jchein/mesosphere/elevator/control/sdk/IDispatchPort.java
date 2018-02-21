package info.jchein.mesosphere.elevator.control.sdk;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

/**
 * 
 * @author jheinnic
 */
public interface IDispatchPort {
	public void assignPickupCall(int floorIndex, DirectionOfTravel departureDirection, int carIndex);
	public void removePickupCall(int floorIndex, DirectionOfTravel departureDirection, int carIndex);
}
