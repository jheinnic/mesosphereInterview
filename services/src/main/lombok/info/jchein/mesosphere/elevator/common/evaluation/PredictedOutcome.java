package info.jchein.mesosphere.elevator.common.evaluation;

import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class PredictedOutcome
{
   private final double fromTime;
   private final double untilTime;

   @Singular
   private final ImmutableMap<CandidateOption, ExperimentOutcome> findings;

   @Singular
   private final ImmutableList<CandidateOption> orderedRecommendations;
   

   public static PredictedOutcome build(Consumer<PredictedOutcomeBuilder> director) {
      PredictedOutcomeBuilder bldr = PredictedOutcome.builder();
      director.accept(bldr);
      return bldr.build();
   }

   public PredictedOutcome copy(Consumer<PredictedOutcomeBuilder> director) {
      PredictedOutcomeBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
