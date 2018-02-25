package info.jchein.mesosphere.elevator.control.manifest.bad;

import lombok.Value;

@Value
public final class BoardingNode implements TravellingNode
{
   private final TravelNodeType nodeType = TravelNodeType.BOARDING;
   private final int fromFloorIndex;
   private final int toFloorIndex = -1;

//   @Override
//   public TravelPathNodeType getPathNodeType()
//   {
//      return TravelPathNodeType.BEFORE_PICK_UP;
//   }
}
