package info.jchein.mesosphere.elevator.common.evaluation;

import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class ExperimentOutcome
{
   @Singular("dataItem")
   private final ImmutableList<PassengerExperience> dataItems;
   private final double medianScore;
   private final double fifthPercentile;
   private final int serviceLevelFailCount;
   private final double weightCost;
   private final double travelDistance;
   
   public static ExperimentOutcome build(Consumer<ExperimentOutcomeBuilder> director) {
      ExperimentOutcomeBuilder bldr = ExperimentOutcome.builder();
      director.accept(bldr);
      return bldr.build();
   }

   public ExperimentOutcome copy(Consumer<ExperimentOutcomeBuilder> director) {
      ExperimentOutcomeBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
