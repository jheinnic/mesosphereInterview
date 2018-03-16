package test.jcop2;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OngoingTraveller
{
   int originFloor;
   int originFloorRelativeIndex;
   double weight;
   
   static OngoingTraveller build(Consumer<OngoingTravellerBuilder> director) {
      final OngoingTravellerBuilder factoryBuilder = OngoingTraveller.builder();
      director.accept(factoryBuilder);
      return factoryBuilder.build();
   }
}
