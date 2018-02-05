package info.jchein.mesosphere.elevator.emulator.model;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

/**
 * IEmulated interfaces are software entities that play the role of physical control elements by providing methods that 
 * expose virtual equivalents to operations one might perform on their physical counterparts, such as pushing buttons, 
 * triggering sensors, opening doors, and so forth.  They provide callable hooks into a software driver that has access
 * to the same access points of the Elevator Group Control infrasturcure that a drive for their hardware equivalents use
 * to convey signals from the hardware of a real world physical deployment.
 *  
 * @author jheinnic
 */
public interface IEmulatedLandingControls
{
   public void pressLandingPickupCall(DirectionOfTravel direction);
}
