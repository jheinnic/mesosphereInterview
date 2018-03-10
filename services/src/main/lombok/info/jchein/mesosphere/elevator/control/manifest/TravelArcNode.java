package info.jchein.mesosphere.elevator.control.manifest;

import lombok.Value;

@Value
public class TravelArcNode implements TravellingNode
{
   private final TravelNodeType nodeType = TravelNodeType.TRAVEL_ARC;
   private final int fromFloorIndex;
   private final int toFloorIndex;

   @Override
   public void printMe(double flowValue, TravellingNode edgeTarget)
   {
      System.out.println(
         String.format("From %s to %s, Flow = %f", this, edgeTarget, flowValue));
   }
}
