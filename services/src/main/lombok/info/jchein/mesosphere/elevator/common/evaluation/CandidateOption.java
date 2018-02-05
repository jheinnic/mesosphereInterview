package info.jchein.mesosphere.elevator.common.evaluation;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class CandidateOption
{
   private final int carIndex;
   private final int floorIndex;
   private final DirectionOfTravel pickupHeading;

   public static CandidateOption build(Consumer<CandidateOptionBuilder> director) {
      CandidateOptionBuilder bldr = CandidateOption.builder();
      director.accept(bldr);
      return bldr.build();
   }

   public CandidateOption copy(Consumer<CandidateOptionBuilder> director) {
      CandidateOptionBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
