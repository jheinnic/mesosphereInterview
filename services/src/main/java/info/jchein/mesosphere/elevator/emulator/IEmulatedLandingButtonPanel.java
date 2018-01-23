package info.jchein.mesosphere.elevator.emulator;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;

/**
 * Not a part of the Elevator SDK, this is an encapsulation of internal implementation details about the Hall Panel's 
 * software driver that allow the scenario simulator to cause the driver to act as though one of its simulated passengers
 * had actually pressed a button on the physical interface.  It does so by injecting a method call that leads to the driver
 * firing the same notification as it would if the hardware interrupt had been genuine.
 *  
 * @author jheinnic
 */
public interface IEmulatedLandingButtonPanel
{
	public int readFloorIndex();
	public void pressCallButton(DirectionOfTravel direction);
}
