package test.jcop;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

@Value
@AllArgsConstructor
public class PassengerEntry implements PassengerVertex
{
   private final int index;
   private final PassengerExchange exchange;
   private final int relativeIndex;
   private final int weightIndex;
   private final int matchCandidateCount;

   @Wither
   private final int firstMatchIndex;
   
   PassengerEntry(int index, PassengerExchange exchange, int relativeIndex, int weightIndex, int matchCandidateCount) {
      this.index = index;
      this.exchange = exchange;
      this.relativeIndex = relativeIndex;
      this.weightIndex = weightIndex;
      this.matchCandidateCount = matchCandidateCount;
      this.firstMatchIndex = -1;
   }

   @Override
   public boolean isEntry()
   {
      return true;
   }

   @Override
   public boolean isExit()
   {
      return false;
   }
   
   public int getFirstOutboundIndex() {
      return this.exchange.getFirstOutboundIndex();
   }
   
   public int getExchangeIndex() {
      return this.exchange.getIndex();
   }
   
   public int getOriginFloor() {
      return this.exchange.getFloor().getFloorIndex();
   }

   @Override
   public String getLabel()
   {
      return String.format("Floor %d, Arriving Passenger %d", this.exchange.getFloor().getFloorIndex(), this.index);
   }
}
