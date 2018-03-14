package test.jcop;

import com.google.common.collect.ImmutableList;

import cz.cvut.felk.cig.jcop.problem.Configuration;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class CandidateSolution
{
   double fitness;
   Configuration configuration;
   @Singular
   ImmutableList<CompletedTrip> completedTrips;
   @Singular
   ImmutableList<OngoingTraveller> ongoingTravellers;
   @Singular
   ImmutableList<ScoreComponent> scoreComponents;
}
