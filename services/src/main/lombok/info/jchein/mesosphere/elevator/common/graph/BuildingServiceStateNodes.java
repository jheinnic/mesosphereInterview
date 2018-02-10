package info.jchein.mesosphere.elevator.common.graph;

import com.google.common.collect.ImmutableList;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class BuildingServiceStateNodes
{
   @Singular
   ImmutableList<CarServiceStateNodes> byCars;

   @Singular
   ImmutableList<FloorServiceStateNodes> byFloors;
}
