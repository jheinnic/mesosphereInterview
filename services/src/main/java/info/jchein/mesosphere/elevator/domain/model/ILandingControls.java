package info.jchein.mesosphere.elevator.domain.model;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;

/**
 * The inward facing interface each software adapter for a hallway control panel provides for the elevator group control's
 * use. 
 * 
 * @author jheinnic
 */
interface ILandingControls {
   void requestPickup(int floorIndex, DirectionOfTravel direction);
   void cancelPickup(int floorIndex, DirectionOfTravel direction);
}
