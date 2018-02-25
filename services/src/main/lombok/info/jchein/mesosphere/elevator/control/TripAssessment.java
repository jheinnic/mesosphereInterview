package info.jchein.mesosphere.elevator.control;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class TripAssessment
{
   private int fromFloorIndex;
   private int toFloorIndex;
   private double weightLoad;
   private long callWaitInterval;
   private long carTravelInterval;
   private long bestTravelInterval;
   
   public static TripAssessment build(Consumer<TripAssessmentBuilder> director)
   {
      final TripAssessmentBuilder bldr = TripAssessment.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public TripAssessment copy(Consumer<TripAssessmentBuilder> director)
   {
      final TripAssessmentBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}

