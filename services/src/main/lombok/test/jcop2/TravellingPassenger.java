package test.jcop2;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TravellingPassenger
{
   final int index;
   final int floorRelativeIndex;
   final int originFloorIndex;
   final int originExchangeIndex;
   double weight;
}
