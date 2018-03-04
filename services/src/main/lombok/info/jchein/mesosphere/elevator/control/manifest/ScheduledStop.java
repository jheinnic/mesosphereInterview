package info.jchein.mesosphere.elevator.control.manifest;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class ScheduledStop implements Comparable<ScheduledStop>

{
   int floorIndex;
   DirectionOfTravel inbound;
   DirectionOfTravel outbound;
   boolean pickUp;
   boolean dropOff;
   
   boolean hasCourseReversal() {
      return this.inbound != this.outbound;
   }

   public static ScheduledStop build(Consumer<ScheduledStopBuilder> director)
   {
      final ScheduledStopBuilder bldr = ScheduledStop.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public ScheduledStop copy(Consumer<ScheduledStopBuilder> director)
   {
      final ScheduledStopBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }

   @Override
   public int compareTo(ScheduledStop o)
   {
      // Implemented only to cause JAssert to expose the AbstractComparable API and allow a Comparator to be used to evaluate
      // isLessThan/isGreaterThan/isEqualTo assertions.  The "natural" order of ScheduledStop alone remains undefined as it 
      // remains dependent on knowledge of the actual direction of travel that is not present in these artifacts.
      return 0;
   }
}
