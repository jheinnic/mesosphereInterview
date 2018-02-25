package info.jchein.mesosphere.elevator.control.manifest.bad;

import lombok.Value;

@Value
public final class TravelArcNode implements TravellingNode
{
   private final TravelNodeType nodeType = TravelNodeType.TRAVEL_ARC;
   private final int fromFloorIndex;
   private final int toFloorIndex;

//   @Override
//   public TravelPathNodeType getPathNodeType()
//   {
//      return TravelPathNodeType.BEFORE_PICK_UP;
//   }
}
