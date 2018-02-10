package info.jchein.mesosphere.elevator.emulator.model;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.RequestId;
import info.jchein.mesosphere.elevator.emulator.event.EmulatorEvent;
import info.jchein.mesosphere.elevator.emulator.event.EmulatorResponse;
import rx.Observable;

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
   public int getNumFloors();
   public int getNumElevators();

	public void callForPickup(int floorIndex, DirectionOfTravel direction);
	public void updateManifest(int carIndex, int floorIndex, DirectionOfTravel direction, Consumer<IManifestUpdate> director);
	public void blockUntil(long clockTime);
}

//	public void pressLandingPickupCall(long clockTime, int floorIndex, DirectionOfTravel direction);
//	public void injectTraveller(long clockTime, int arrivalFloor, int destinationFloor);
