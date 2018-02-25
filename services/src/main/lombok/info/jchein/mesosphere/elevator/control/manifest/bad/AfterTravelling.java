package info.jchein.mesosphere.elevator.control.manifest.bad;

import lombok.Value;

@Value
public final class AfterTravelling implements TravellingNode
{
   private final TravelNodeType nodeType = TravelNodeType.AFTER_TRAVEL;
   private final int fromFloorIndex = -1;
   private final int toFloorIndex = -1;

//   @Override
//   public TravelPathNodeType getPathNodeType()
//   {
//      return TravelPathNodeType.AFTER_DROP_OFF;
//   }
}
