package info.jchein.mesosphere.elevator.common.graph;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(doNotUseGetters=true)
public class DirectedAdjacency
{
   int fromFloorIndex;
   int toFloorIndex;
}
