package info.jchein.mesosphere.elevator.domain.sdk;

import java.util.BitSet;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;

/**
 * 
 * @author jheinnic
 */
public interface IElevatorSchedulerPort {
	public void updateDestinationQueue(
	   int carIndex, DirectionOfTravel initialDirection, int reverseAfter, BitSet upwardStops, BitSet downwardStops
   );
}
