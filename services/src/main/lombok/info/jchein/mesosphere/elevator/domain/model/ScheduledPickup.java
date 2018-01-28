package info.jchein.mesosphere.elevator.domain.model;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScheduledPickup implements ScheduledStop
{
   private final int floorIndex;
   private final DirectionOfTravel direction;
   
   public boolean hasDirection() {
      return true;
   }
}
