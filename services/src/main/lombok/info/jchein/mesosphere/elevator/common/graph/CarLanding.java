package info.jchein.mesosphere.elevator.common.graph;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(doNotUseGetters=true)
/**
 * Graph node represening a specific car located at a specific floor in a travel direction-agnostic way.  
 * 
 * For problems that need to represent a specific car at a specific floor in more than one distinct state, there are other node classes in this package that
 * may yield a suitable vertex count to their "all" notion. 
 * @{link ElevatorStatus}.
 * 
 * {@link CarPickupHeading} has twice as many instances and distinguishes ascent from descent.  {@link CarTravelType} has three times as many instances,
 * and distinguishes between ascending, descending, and stationary.  Finally, {@link CarServiceState} has five times as many nodes, and distinguishes between
 * ascent, descent, parked, boarding, and idle.
 * 
 * @author jheinnic
 *
 */
public class CarLanding
{
   private final int floorIndex;
   private final int carIndex;
}
