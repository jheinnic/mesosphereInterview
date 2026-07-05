package test.jcop2;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@AllArgsConstructor
@EqualsAndHashCode(doNotUseGetters=true, of= {"index"})
public class PassengerExchange
{
   final int index;
   final int floorIndex;
   final int floorRelativeIndex;
   final int firstIncomingIndex;
   final int passengersIn;
   final int passengersOut;
   final double expectedWeightChange;
   
   PassengerExchange(int index, FloorLanding floor, int floorRelativeIndex, BoardingEvent event, int firstIncomingIndex)
   {
      this.index = index;
      this.floorIndex = floor.getFloorIndex();
      this.floorRelativeIndex = floorRelativeIndex;
      this.firstIncomingIndex = firstIncomingIndex;
      this.passengersIn = event.getPassengersIn();
      this.passengersOut = event.getPassengersOut();
      this.expectedWeightChange = event.getWeightChange();
   }
}
