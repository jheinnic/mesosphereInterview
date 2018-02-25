package info.jchein.mesosphere.elevator.control;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class SegmentWeightCost
{
   int fromFloor;
   int toFloor;
   DirectionOfTravel direction;
   double minWeightLoad;
   double maxWeightLoad;
   
   public static SegmentWeightCost build(Consumer<SegmentWeightCostBuilder> director)
   {
      final SegmentWeightCostBuilder bldr = SegmentWeightCost.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public SegmentWeightCost copy(Consumer<SegmentWeightCostBuilder> director)
   {
      final SegmentWeightCostBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
