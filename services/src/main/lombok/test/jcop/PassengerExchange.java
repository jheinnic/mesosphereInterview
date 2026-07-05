package test.jcop;

import org.apache.commons.math3.analysis.function.Gaussian;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@AllArgsConstructor
@EqualsAndHashCode(doNotUseGetters=true, of= {"index"})
public class PassengerExchange
{
   final int index;
   final FloorLanding floor;
   final int passengersIn;
   final int passengersOut;
   final int firstInboundIndex;
   final int firstOutboundIndex;
   final double expectedIncomingWeight;
   final double expectedOutgoingWeight;
   final double expectedWeightChange;
   final private Gaussian actualChangeGauss;
   
   PassengerExchange(int index, FloorLanding floor, BoardingEvent event, int firstInboundIndex, int firstOutboundIndex, double expectedIncomingWeight) {
      this.index = index;
      this.floor = floor;
      this.passengersIn = event.getPassengersIn();
      this.passengersOut = event.getPassengersOut();
      this.firstInboundIndex = firstInboundIndex;
      this.firstOutboundIndex = firstOutboundIndex;
      this.expectedIncomingWeight = expectedIncomingWeight;
      this.expectedOutgoingWeight = expectedIncomingWeight + event.getWeightChange();
      this.expectedWeightChange = event.getWeightChange();
      this.actualChangeGauss = new Gaussian(1.0, this.expectedWeightChange, 9.5);
   }
   
   /**
    * Corner case constructor for a pseudo-exchange representing the overflow passenger complement for
    * ongoing travel not yet charted.
    * 
    * @param passengersRemaining
    * @param expectedIncomingWeight
    */
   PassengerExchange(int index, int passengersRemaining, int firstInboundIndex, int firstOutboundIndex, double expectedIncomingWeight) {
      this.index = index;
      this.floor = null;
      this.passengersIn = 0;
      this.passengersOut = passengersRemaining;
      this.firstInboundIndex = firstInboundIndex;
      this.firstOutboundIndex = firstOutboundIndex;
      this.expectedIncomingWeight = expectedIncomingWeight;
      this.expectedOutgoingWeight = 0.0;
      this.expectedWeightChange = 0.0 - expectedIncomingWeight;
      this.actualChangeGauss = new Gaussian(1.0, this.expectedWeightChange, 100);
   }
   
   public double getPartialScore(double actualChange) {
      return this.actualChangeGauss.value(actualChange);
   }
}
