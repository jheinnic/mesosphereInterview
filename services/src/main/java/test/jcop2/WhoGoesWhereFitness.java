package test.jcop2;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.analysis.function.Gaussian;

import com.google.common.collect.ImmutableList;

import cz.cvut.felk.cig.jcop.problem.BaseFitness;
import cz.cvut.felk.cig.jcop.problem.Configuration;
import cz.cvut.felk.cig.jcop.problem.Fitness;
import cz.cvut.felk.cig.jcop.util.JcopRandom;
import info.jchein.mesosphere.elevator.common.probability.IPopulationSampler;
import lombok.extern.slf4j.Slf4j;
import test.jcop2.CandidateSolution.CandidateSolutionBuilder;


/**
 * Default fitness for Who Went Where problem
 *
 * @author John Heinnickel
 */
@Slf4j
public abstract class WhoGoesWhereFitness
extends BaseFitness
implements Fitness
{
   private static double ATTR_SCALE = 1.0 / Integer.MAX_VALUE;

   /**
    * Problem for which to calculate fitness.
    */
   protected final IWhoGoesWhereProblem problem;
   protected final IPopulationSampler populationSampler;
   protected final ImmutableList<FitnessExchange> fitnessExchanges;
   protected final ArrayList<TravellingPassenger> onboardPassengers;
   protected final List<TravellingPassenger> allArrivals;
   private final FitnessExchange ongoingExchange;
   
   private int testCounter = 0;

   /**
    * Fitness requires source problem instance to be able to calculate
    *
    * @param problem
    *           reference to problem so to be able to calculate
    * @param populationSampler
    */
   public WhoGoesWhereFitness( IWhoGoesWhereProblem problem, IPopulationSampler populationSampler )
   {
      this.problem = problem;
      this.populationSampler = populationSampler;

      /* BaseFitness */
      this.asymmetricScale = false;
      this.maxFitness = 1.0;
      this.minFitness = 0.0;

      this.fitnessExchanges = ImmutableList.<FitnessExchange>builder()
         .addAll(
            problem.getExchanges()
               .stream()
               .map(this::allocateFitnessExchange)
               .iterator()
         ).build();

      this.onboardPassengers =
         new ArrayList<TravellingPassenger>(
            problem.getTravellerCount());
      this.allArrivals =
         this.fitnessExchanges.stream().flatMap((exchange) -> {
            return exchange.getArrivals().stream();
         }).collect(Collectors.toList());
         new ArrayList<TravellingPassenger>(
            problem.getTravellerCount());
      
      final int passengersOnBoard = problem.getPassengersOnBoard();
      if (passengersOnBoard > 0) {
	      final double weightRemaining =
	         0 - this.fitnessExchanges.stream().collect(
	            Collectors.summingDouble(FitnessExchange::getExpectedWeightChange));
	      this.ongoingExchange =
	         FitnessExchange.builder()
	            .passengersOut(passengersOnBoard)
	            .expectedWeightChange(weightRemaining)
	            .scoreFunction(
	               this.getScoreFunction(weightRemaining, 0, passengersOnBoard)
	            ).build();
      } else {
         this.ongoingExchange = null;
      }
   }

   protected FitnessExchange allocateFitnessExchange(PassengerExchange nextExchange)
   {
      final FitnessExchange.FitnessExchangeBuilder builder = FitnessExchange.builder();

      final double expectedWeightChange = nextExchange.getExpectedWeightChange();
      final int floorIndex = nextExchange.getFloorIndex();
      final int passengersIn = nextExchange.getPassengersIn();
      final int passengersOut = nextExchange.getPassengersOut();

      int nextFloorIndex = nextExchange.getFloorRelativeIndex();
      int nextIndex = nextExchange.getFirstIncomingIndex();

      for (int ii = 0; ii < passengersIn; ii++) {
         builder.arrival(
            TravellingPassenger.builder()
               .index(nextIndex++)
               .floorRelativeIndex(nextFloorIndex++)
               .originFloorIndex(nextExchange.getFloorIndex())
               .originExchangeIndex(nextExchange.getIndex())
               .build());
      }

      return builder.passengersOut(passengersOut)
         .expectedWeightChange(expectedWeightChange)
         .floorIndex(floorIndex)
         .scoreFunction(
            this.getScoreFunction(expectedWeightChange, passengersIn, passengersOut)
         ).build();
   }

   protected abstract Gaussian getScoreFunction(double weightDelta, int passengersIn, int passengersOut);
   
   /**
    * Default fitness for who went where algorithm.
    * <p/>
    * Returns sum of gaussian error score from each PassengerExchange segment after attributes are used to assign each
    * incoming passenger a weight and match probability with each other potential outbound passenger.
    * <p/>
    *
    * @param configuration
    *           attributes to compute fitness
    * @return fitness of attributes
    */
   public double getValue(Configuration configuration)
   {
//      return this.transformConfiguration(configuration).getFitness();
      CandidateSolution retval = this.transformConfiguration(configuration);
      if ((this.testCounter <= 210) && (this.testCounter++ > 200)) {
         log.info("Solution {} is {}", this.testCounter, retval.toString());
      }
      return retval.getFitness();
   }


   public CandidateSolution transformConfiguration(Configuration configuration)
   {
      final CandidateSolutionBuilder solutionBuilder = CandidateSolution.builder();
      solutionBuilder.configuration(configuration);

      // To avoid breaking JCOP's own randomization, take a value from the current sequence to use as seed after
      // we've read some values using seed from our configuration.
      final long cachedSeed = JcopRandom.nextLong();

      // Use the first two attributes to seed random number generator for sampling weights.
      final int lowSeed = configuration.valueAt(0);
      final int highSeed = configuration.valueAt(1);
      final long seedAttr = (((long) highSeed) << 32) | (lowSeed & 0xffffffffL);
      JcopRandom.setSeed(seedAttr);

      // Next, use the (numTravellers-1) to order the first N-1 passengers to receive weights
      // from the population sampler using the seeded random number generator.
      int lotteryAttrIndex = this.problem.getFirstLotteryIndex();
      final ArrayList<TravellingPassenger> passengersWithoutWeight =
         new ArrayList<TravellingPassenger>(this.allArrivals);
      for (int weightAttrIndex = 2; weightAttrIndex < lotteryAttrIndex; weightAttrIndex++) {
         final int nextIndex = configuration.valueAt(weightAttrIndex);
         final TravellingPassenger nextPassenger = passengersWithoutWeight.get(nextIndex);
         nextPassenger.setWeight(
            this.populationSampler.lookup(
               JcopRandom.nextDouble(),
               JcopRandom.nextDouble()));
         passengersWithoutWeight.remove(nextIndex);
      }
      
      // One passenger remains to be assigned weight
      passengersWithoutWeight.get(0)
         .setWeight(
            this.populationSampler.lookup(
               JcopRandom.nextDouble(),
               JcopRandom.nextDouble()));

      // Finally, use the last (completedTrips) attributes to assign each arriving passenger to its destination
      // The attributes maximums have been set such that every onboard passenger is a candidate provided that
      // FitnessExchanges are processed in traversal order, with departures occuring before arrivals at each
      // exchange.
      this.onboardPassengers.clear();
      for (final FitnessExchange nextExchange : this.fitnessExchanges) {
         final int passengersOut = nextExchange.getPassengersOut();
         final ImmutableList<TravellingPassenger> arrivals = nextExchange.getArrivals();
         final ArrayList<TravellingPassenger> departures = nextExchange.getDepartures();
         
         for (int ii = 0; ii<passengersOut; ii++ ) {
            final int nextDepartureIndex = configuration.valueAt(lotteryAttrIndex++);
            final TravellingPassenger nextDeparture = 
               this.onboardPassengers.get(nextDepartureIndex);
            this.onboardPassengers.remove(nextDepartureIndex);

            departures.add(nextDeparture);
            solutionBuilder.completedTrip((builder) -> {
               builder.originFloor(nextDeparture.getOriginFloorIndex())
               .originFloorRelativeIndex(nextDeparture.getFloorRelativeIndex())
               .destinationFloor(nextExchange.getFloorIndex())
               .weight(nextDeparture.getWeight());
            });
         }
         
         // Having just processed the departures, queue up the arrivals.  The next attribute will have been sampled
         // from a variable that reflects this increase in candidate count.
         this.onboardPassengers.addAll(arrivals);
      }
      
      // Anyone still onboard is should be accounted for by remaining weight in elevator car.  Account for it with a
      // pretend exchange that imagines that many passengers of that much weight disembark at an imaginary next stop
      // that has not actually happened yet.
      for (TravellingPassenger nextTraveller: this.onboardPassengers) {
         solutionBuilder.ongoingTraveller((builder) -> {
            builder.originFloor(nextTraveller.getOriginFloorIndex())
               .originFloorRelativeIndex(nextTraveller.getFloorRelativeIndex())
               .weight(nextTraveller.getWeight());
         });
      }

      // Calculate partial scores and clear the transient state so the score tracking structures may be reused.
      for (final FitnessExchange nextExchange : this.fitnessExchanges) {
         solutionBuilder.scoreComponent(
            nextExchange.computeResult()
         );
      }
      if (this.ongoingExchange != null) {
         // Only tabulate the final imaginary exchange if there were any passengers left on board.  The error function
         // does not work well when the expected value and standard deviation are zero.
         this.ongoingExchange.getDepartures()
            .addAll(this.onboardPassengers);
         solutionBuilder.scoreComponent(
            this.ongoingExchange.computeResult());
      }

      // JcopRandom.setSeed(System.currentTimeMillis());
      JcopRandom.setSeed(cachedSeed);
      return solutionBuilder.build();
   }
}
