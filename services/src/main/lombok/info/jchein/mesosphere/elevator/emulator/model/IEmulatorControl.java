package info.jchein.mesosphere.elevator.emulator.model;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsOpened;

/**
 * IEmulated interfaces are software entities that play the role of physical control elements by providing methods that 
 * expose virtual equivalents to operations one might perform on their physical counterparts, such as pushing buttons, 
 * triggering sensors, opening doors, and so forth.  They provide callable hooks into and process feedback signals out from
 * a software driver that has access identical to what the software for their hardware-based counerparts do.as
 * to convey signals from the hardware of a real world physical deployment.
 *  
 * @author jheinnic
 */
public interface IEmulatorControl
{
//   public int getNumFloors();
//   public int getNumElevators();

	public void callForPickup(int floorIndex, DirectionOfTravel direction);
	
	/**
	 * API method for use when handling a {@link PassengerDoorsOpened} event to perform all required boarding,
	 * disembarking, and drop off requesting.
	 * 
	 * @param carIndex
	 * @param floorIndex
	 * @param direction
	 * @param onPickupCallback
	 * @throws IllegalArgumentException if the car at {@code carIndex} is not currently at {@code floorIndex} with
	 *                                  its doors open for boarding in {@code direction}, IllegalArgumentException
	 *                                  will be thrown and no call to the 
	 */
	public void updateManifest(int carIndex, int floorIndex, DirectionOfTravel direction, Consumer<IManifestUpdate> onPickupCallback);
	public void blockUntil(long clockTime);
}

//	public void pressLandingPickupCall(long clockTime, int floorIndex, DirectionOfTravel direction);
//	public void injectTraveller(long clockTime, int arrivalFloor, int destinationFloor);
