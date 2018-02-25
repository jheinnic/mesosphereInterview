package info.jchein.mesosphere.elevator.control;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class FitnessScore
{
   double pickupTimeScore;
   
   double travelTimeScore;
   
   double efficiencyScore;
   
   double totalFitness;
   
   public static FitnessScore build(Consumer<FitnessScoreBuilder> director)
   {
      final FitnessScoreBuilder bldr = FitnessScore.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public FitnessScore copy(Consumer<FitnessScoreBuilder> director)
   {
      final FitnessScoreBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
