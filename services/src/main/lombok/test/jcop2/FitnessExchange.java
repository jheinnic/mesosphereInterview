
package test.jcop2;


import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.commons.math3.analysis.function.Gaussian;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;


@Value
class FitnessExchange
{
   private final int floorIndex;
   private final double expectedWeightChange;
   private final int passengersOut;

   @Singular
   private final ImmutableList<TravellingPassenger> arrivals;
   private final ArrayList<TravellingPassenger> departures;
   private final Gaussian scoreFunction;
   
   @Builder
   static FitnessExchange makeFitnessExchange(
      int floorIndex, double expectedWeightChange, int passengersOut, 
      @Singular ImmutableList<TravellingPassenger> arrivals, Gaussian scoreFunction)
   {
      return new FitnessExchange(
         floorIndex, expectedWeightChange, passengersOut, arrivals, new ArrayList<TravellingPassenger>(passengersOut), scoreFunction);
   }

   ScoreComponent computeResult()
   {
      Preconditions.checkState(this.departures.size() == this.passengersOut);

      final double actualWeightChange =
         this.arrivals.stream()
            .collect(Collectors.summingDouble(TravellingPassenger::getWeight)) -
         this.departures.stream()
            .collect(Collectors.summingDouble(TravellingPassenger::getWeight));
      this.departures.clear();

      return ScoreComponent.builder()
         .expectedWeightChange(this.expectedWeightChange)
         .actualWeightChange(actualWeightChange)
         .partialScore(
            this.scoreFunction.value(actualWeightChange)
         ).build();
   }
}
