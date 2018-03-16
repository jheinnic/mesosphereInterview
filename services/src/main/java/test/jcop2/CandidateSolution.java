package test.jcop2;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import cz.cvut.felk.cig.jcop.problem.Configuration;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
public class CandidateSolution
{
   double fitness;
   Configuration configuration;
   ImmutableList<CompletedTrip> completedTrips;
   ImmutableList<OngoingTraveller> ongoingTravellers;
   ImmutableList<ScoreComponent> scoreComponents;
   
   @Builder
   static CandidateSolution makeSolution(
      Configuration configuration,
      @Singular ImmutableList<Consumer<CompletedTrip.CompletedTripBuilder>> completedTrips, 
      @Singular ImmutableList<Consumer<OngoingTraveller.OngoingTravellerBuilder>> ongoingTravellers, 
      @Singular ImmutableList<ScoreComponent> scoreComponents
   ) {
      return new CandidateSolution(
         scoreComponents.stream().collect(
            Collectors.summingDouble(ScoreComponent::getPartialScore)) / scoreComponents.size(),
         configuration,
         ImmutableList.<CompletedTrip>builder().addAll(
            completedTrips.stream().map(CompletedTrip::build).iterator()
         ).build(),
         ImmutableList.<OngoingTraveller>builder().addAll(
            ongoingTravellers.stream().map(OngoingTraveller::build).iterator()
         ).build(),
         scoreComponents
      );
   }
}
