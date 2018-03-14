package info.jchein.mesosphere.elevator.control.manifest;

import lombok.Value;

@Value
public final class OnboardPassenger implements PassengerNode
{
   private final PassengerNodeType nodeType = PassengerNodeType.DROP_OFF;
   private final int floorIndex;

   @Override
   public void printMe(double flowValue, PassengerNode matchedDeboard)
   {
      System.out.println(
         String.format("From %s to %s, Flow = %f", this, matchedDeboard, flowValue));
   }
}
