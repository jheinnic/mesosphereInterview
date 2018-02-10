package info.jchein.mesosphere.elevator.common.graph;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(doNotUseGetters=true)
public class BuildingCar implements ISchedulingVertex
{
   @Override
   public SchedulingNodeType getNodeType()
   {
      return SchedulingNodeType.CAR_TERMINAL;
   }

   private final int carIndex;
}
