package info.jchein.mesosphere.elevator.control.manifest;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
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
