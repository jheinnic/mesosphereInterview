package info.jchein.mesosphere.elevator.common.graph;

import com.google.common.collect.ImmutableList;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class FloorServiceStateNodes
{
   int floorIndex;
   ImmutableList<CarFloorServiceStatesGroup> byCars;
}
