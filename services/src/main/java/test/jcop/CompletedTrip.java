package test.jcop;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CompletedTrip
{
   int index;
   int originFloor;
   int destinationFloor;
   double weight;
}
