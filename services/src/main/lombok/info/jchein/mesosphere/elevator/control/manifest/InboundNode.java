package info.jchein.mesosphere.elevator.control.manifest;

import lombok.Value;

@Value
public final class InboundNode implements TravellingNode
{
   private final TravelNodeType nodeType = TravelNodeType.INBOUND_NODE;

   @Override
   public void printMe(double flowValue, TravellingNode edgeTarget)
   {
      System.out.println(
         String.format("From %s to %s, Flow = %f", this, edgeTarget, flowValue));
   }
}
