package info.jchein.mesosphere.elevator.domain.emulator;

import java.util.function.Consumer;
import java.util.function.Function;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;

/**
 * IEmulated interfaces are software entities that play the role of physical control elements by providing methods that 
 * expose virtual equivalents to operations one might perform on their physical counterparts, such as pushing buttons, 
 * triggering sensors, opening doors, and so forth.  Their 
 * software driver that allow the scenario simulator to cause the driver to act as though one of its simulated passengers
 * had actually pressed a button on the physical interface.  It does so by injecting a method call that leads to the driver
 * firing the same notification as it would if the hardware interrupt had been genuine.
 *  
 * @author jheinnic
 */
public interface ISimulationScenario
{
	public void pressPassengerDropCall(long clockTime, int carIndex, int floorIndex);
	public void pressLandingPickupCall(long clockTime, int floorIndex, DirectionOfTravel direction);
	public void injectTraveller(long clockTime, int arrivalFloor, int destinationFloor);
}
