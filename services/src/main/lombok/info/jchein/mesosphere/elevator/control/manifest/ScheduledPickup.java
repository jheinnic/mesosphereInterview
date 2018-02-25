package info.jchein.mesosphere.elevator.control.manifest;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
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
