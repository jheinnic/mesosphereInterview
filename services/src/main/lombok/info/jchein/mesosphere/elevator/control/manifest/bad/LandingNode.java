package info.jchein.mesosphere.elevator.control.manifest.bad;

import lombok.Value;

@Value
public final class LandingNode implements TravellingNode
{
   private final TravelNodeType nodeType = TravelNodeType.LANDING;
   private final int fromFloorIndex = -1;
   private final int toFloorIndex;

//   @Override
//   public TravelPathNodeType getPathNodeType()
//   {
//      return TravelPathNodeType.BEFORE_PICK_UP;
//   }
}
