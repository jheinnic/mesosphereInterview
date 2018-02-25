package info.jchein.mesosphere.elevator.control.manifest.bad;

import lombok.Value;

@Value
public final class BeforeTravelling implements TravellingNode
{
   private final TravelNodeType nodeType = TravelNodeType.BEFORE_TRAVEL;
   private final int fromFloorIndex = -1;
   private final int toFloorIndex = -1;
//   @Override
//   public TravelPathNodeType getPathNodeType()
//   {
//      return TravelPathNodeType.BEFORE_PICK_UP;
//   }
}
