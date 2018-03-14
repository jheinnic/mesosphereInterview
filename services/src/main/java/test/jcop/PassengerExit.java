package test.jcop;

import lombok.Value;

@Value
public class PassengerExit implements PassengerVertex
{
   final int index;
   final PassengerExchange exchange;
   final int relativeIndex;

   @Override
   public boolean isEntry()
   {
      return false;
   }

   @Override
   public boolean isExit()
   {
      return true;
   }
   
   public int getExchangeIndex() {
      return this.exchange.getIndex();
   }

   public int getDestinationFloor() {
      return this.exchange.getFloor().getFloorIndex();
   }

   @Override
   public String getLabel()
   {
      if (this.exchange.getFloor() != null) {
         return String.format("Floor %d, Departing Passenger %d", this.exchange.getFloor().getFloorIndex(), this.index);
      } else {
         return String.format("Ongoing Traveller %d", this.relativeIndex);
      }
   }

   /**
    * @return True if the PassengerExchange for this outbound node is associated with a destination floor, and therefore represents arrival at a destination floor.
    * Otherwise, false if the PassengerExchange has a null FloorLanding and therefore represents a passenger still travelling in the elevator car.
    */
   public boolean hasReachedDestination()
   {
      return this.exchange.getFloor() != null;
   }
}
