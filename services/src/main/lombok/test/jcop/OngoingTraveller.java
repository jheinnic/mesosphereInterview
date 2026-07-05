package test.jcop;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OngoingTraveller
{
   int index;
   int originFloor;
   double weight;
}
