package info.jchein.mesosphere.elevator.common.graph;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * A LocationIntent is a node that captures a present and future location pair.  There is no restriction on valid floor pairs.  Use this instance for
 * graphs that need a vertex count bounded by the cartesian product, Floor X Floor.  The distinct case where from and to are the same floor represents an
 * explicit decision to go nowhere.  Both floor indices must be valid for the host building (e.g. neither negative nor greater than the highest known floor.)
 * 
 * More common use cases will be interested in either floor pairs that must not be identical.  Vertex classes with this semantic difference are found
 * in {@link TravelArc}, for all non-identical floor pairs, and {@link DirectedAdjacency}, for immediately adjacent floor pairs only.
 * 
 * @author jheinnic
 */
@Value
@EqualsAndHashCode(doNotUseGetters=true)
public class PassengersInTransit
{
   private int boardingFloor;
   private int ridingFromFloor;
   private int ridingToFloor;
}
