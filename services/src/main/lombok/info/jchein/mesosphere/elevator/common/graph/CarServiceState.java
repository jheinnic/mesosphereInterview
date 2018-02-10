package info.jchein.mesosphere.elevator.common.graph;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.ServiceLifecycleStage;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Graph node represening a specific car either stopped or travelling through a specifc floor.
 * 
 * @author jheinnic
 */
@Value
@EqualsAndHashCode(doNotUseGetters=true)
public class CarServiceState implements ISchedulingVertex
{
   public SchedulingNodeType getNodeType() {
      return SchedulingNodeType.PATH_TRACKING;
   }

   @Min(0)
   private final int floorIndex;

   @Min(0)
   private final int carIndex;

   @NotNull
   private final ServiceLifecycleStage stage;

   @NotNull
   private final DirectionOfTravel direction;
}
