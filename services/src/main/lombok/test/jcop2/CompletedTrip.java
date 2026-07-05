package test.jcop2;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CompletedTrip
{
   int originFloor;
   int originFloorRelativeIndex;
   int destinationFloor;
   double weight;
   
   static CompletedTrip build(Consumer<CompletedTripBuilder> director) {
      final CompletedTripBuilder factoryBuilder = CompletedTrip.builder();
      director.accept(factoryBuilder);
      return factoryBuilder.build();
   }
}
