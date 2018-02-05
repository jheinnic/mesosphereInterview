package info.jchein.mesosphere.elevator.common.graph;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Although similar in structure to FloorAdjacency, there is a semantic distinction.  TravelArcs exist between any two different BuildingFloors,
 * but FloorAdjacency nodes are only created between adjacent floors.  This distinction exists for use cases that begin with a "set of all vertices"
 * requirement, which may either intend "all" to include all pairs of floors, regardless of distance, or all adjacent floor pairs.
 * 
 * Neither 
 * @author jheinnic
 */
@Value
@EqualsAndHashCode(doNotUseGetters=true)
public class TravelArc
{
   private int fromFloorIndex;
   private int toFloorIndex;
}
