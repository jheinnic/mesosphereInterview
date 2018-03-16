package test.jcop2;


import java.util.ArrayList;
import java.util.stream.Collectors;

import org.apache.commons.math3.analysis.function.Gaussian;

import com.google.common.collect.ImmutableList;

import cz.cvut.felk.cig.jcop.problem.BaseFitness;
import cz.cvut.felk.cig.jcop.problem.Configuration;
import cz.cvut.felk.cig.jcop.problem.Fitness;
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
   private final FitnessExchange ongoingExchange;

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
      
      final int passengersOnBoard = problem.getPassengersOnBoard();
      if (passengersOnBoard > 0) {
	      final double weightRemaining =
	         this.fitnessExchanges.stream().collect(
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
      return this.transformConfiguration(configuration).getFitness();
//      CandidateSolution retval = this.transformConfiguration(configuration);
//      if ((this.testCounter++ % 200) == 0) {
//         log.info("Solution {} is {}", this.testCounter, retval.toString());
//      }
//      return retval.getFitness();
   }


   public CandidateSolution transformConfiguration(Configuration configuration)
   {
      final CandidateSolutionBuilder solutionBuilder = CandidateSolution.builder();
      solutionBuilder.configuration(configuration);

      this.onboardPassengers.clear();
      int weightAttrIndex = 0;
      int lotteryAttrIndex = this.problem.getFirstLotteryIndex();

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

         for (final TravellingPassenger nextPassenger : arrivals) {
            /*final int lowSeed = configuration.valueAt(weightAttrIndex++);
            final int highSeed = configuration.valueAt(weightAttrIndex++);
            final long seedAttr = (((long) highSeed) << 32) | (lowSeed & 0xffffffffL);
            JcopRandom.setSeed(seedAttr);

            nextPassenger.setWeight(
               this.populationSampler.lookup(
                  JcopRandom.nextDouble(),
                  JcopRandom.nextDouble()));*/
            final double probGroup = configuration.valueAt(weightAttrIndex++) * ATTR_SCALE;
            final double probDist = configuration.valueAt(weightAttrIndex++) * ATTR_SCALE;
            nextPassenger.setWeight(
               this.populationSampler.lookup(probGroup, probDist));
         }
         
         this.onboardPassengers.addAll(arrivals);
      }
      
      for (TravellingPassenger nextTraveller: this.onboardPassengers) {
         solutionBuilder.ongoingTraveller((builder) -> {
            builder.originFloor(nextTraveller.getOriginFloorIndex())
               .originFloorRelativeIndex(nextTraveller.getFloorRelativeIndex())
               .weight(nextTraveller.getWeight());
         });
      }

      for (final FitnessExchange nextExchange : this.fitnessExchanges) {
         solutionBuilder.scoreComponent(
            nextExchange.computeResult()
         );
      }
      if (this.ongoingExchange != null) {
         this.ongoingExchange.getDepartures()
            .addAll(this.onboardPassengers);
         solutionBuilder.scoreComponent(
            this.ongoingExchange.computeResult());
      }

      return solutionBuilder.build();
   }
}
