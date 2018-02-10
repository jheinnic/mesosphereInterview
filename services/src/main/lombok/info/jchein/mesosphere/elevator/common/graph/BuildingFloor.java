package info.jchein.mesosphere.elevator.common.graph;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(doNotUseGetters=true)
public class BuildingFloor implements ISchedulingVertex
{
   @Override
   public SchedulingNodeType getNodeType()
   {
      return SchedulingNodeType.FLOOR_TERMINAL;
   }

   private final int floorIndex;
}
