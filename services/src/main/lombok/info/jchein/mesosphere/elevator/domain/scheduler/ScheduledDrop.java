package info.jchein.mesosphere.elevator.domain.scheduler;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScheduledDrop implements ScheduledStop
{
   int floorIndex;
   
   public boolean hasDirection() {
      return false;
   }
   
   public DirectionOfTravel getDirection() {
      return null;
   }
}
