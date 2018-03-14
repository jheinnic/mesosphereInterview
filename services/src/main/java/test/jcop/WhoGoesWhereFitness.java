package test.jcop;


import org.apache.commons.math3.util.MathArrays;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.Matching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.builder.GraphBuilder;

import cz.cvut.felk.cig.jcop.problem.BaseFitness;
import cz.cvut.felk.cig.jcop.problem.Configuration;
import cz.cvut.felk.cig.jcop.problem.Fitness;
import info.jchein.mesosphere.elevator.common.probability.IPopulationSampler;
import lombok.extern.slf4j.Slf4j;
import test.jcop.CandidateSolution.CandidateSolutionBuilder;


/**
 * Default fitness for Knapsack problem.
 *
 * @author Ondrej Skalicka
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
      this.maxFitness = problem.getExchanges().length;
      this.minFitness = 0;
      this.asymmetricScale = false;
   }


   protected abstract
   GraphBuilder<PassengerVertex, DefaultWeightedEdge, SimpleWeightedGraph<PassengerVertex, DefaultWeightedEdge>>
   getGraphBuilder();


   protected abstract Matching<PassengerVertex, DefaultWeightedEdge>
   getOptimalMatching(SimpleWeightedGraph<PassengerVertex, DefaultWeightedEdge> graph);


   /**
    * Default fitness for knapsack algorithm.
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
      CandidateSolution candidate = transformConfiguration(configuration);
      return candidate.getFitness();
   }


   public CandidateSolution transformConfiguration(Configuration configuration)
   {
      final PassengerEntry[] arrivals = this.problem.getArrivals();
      final PassengerExit[] departures = this.problem.getDepartures();
      final PassengerExchange[] exchanges = this.problem.getExchanges();
      final CandidateSolutionBuilder solutionBuilder = CandidateSolution.builder();
      solutionBuilder.configuration(configuration);

      final int passengerCount = arrivals.length;
      final int exchangeCount = exchanges.length;

      final double[] passengerWeights = new double[passengerCount];
      final double[] weightChanges = new double[exchangeCount];

      final GraphBuilder<PassengerVertex, DefaultWeightedEdge, SimpleWeightedGraph<PassengerVertex, DefaultWeightedEdge>> graphBuilder =
         this.getGraphBuilder();
      graphBuilder.addVertices(arrivals)
         .addVertices(departures);

      for (int ii = 0; ii < passengerCount; ii++) {
         final PassengerEntry nextEntry = arrivals[ii];
         final int exchangeIndex = nextEntry.getExchangeIndex();
         final int weightIndex = nextEntry.getWeightIndex();
         final double attrValueOne = configuration.valueAt(weightIndex) * ATTR_SCALE;
         final double attrValueTwo = configuration.valueAt(weightIndex + 1) * ATTR_SCALE;
         passengerWeights[ii] = this.populationSampler.lookup(attrValueOne, attrValueTwo);
         weightChanges[exchangeIndex] += passengerWeights[ii];

         final int firstMatchIndex = nextEntry.getFirstMatchIndex();
         final int firstOutboundIndex = nextEntry.getFirstOutboundIndex();
         final int matchCandidateCount = nextEntry.getMatchCandidateCount();
//         log.info("Loading match algorithm with edges origin {} to {} destinations from {}({}) to {}({})", nextEntry.getLabel(), matchCandidateCount, departures[firstOutboundIndex].getLabel(),
//            firstOutboundIndex, departures[firstOutboundIndex + matchCandidateCount - 1].getLabel(), firstOutboundIndex + matchCandidateCount - 1);
         double[] matchWeights = new double[matchCandidateCount];
         for (int jj = 0, kk = firstMatchIndex; jj < matchCandidateCount; jj++, kk++) {
            matchWeights[jj] = configuration.valueAt(kk) * ATTR_SCALE;
         }
         matchWeights = MathArrays.normalizeArray(matchWeights, 1000.00);
         for (int jj = 0, kk = firstOutboundIndex; jj < matchCandidateCount; jj++, kk++) {
            final PassengerExit nextExit = departures[kk];
            graphBuilder.addEdge(nextEntry, nextExit, matchWeights[jj]);
            // log.info(String.format("Adding edge from %s to %s of weight %f", nextEntry.getLabel(),
            // nextExit.getLabel(), matchWeights[jj]));
         }
      }
      final SimpleWeightedGraph<PassengerVertex, DefaultWeightedEdge> graph = graphBuilder.build();
      final Matching<PassengerVertex, DefaultWeightedEdge> matching = this.getOptimalMatching(graph);
      if (matching.isPerfect()) {
         // log.info("Fitness check computed perfect matching for candidate graph");
      } else {
         log.warn("Fitness check computed imperfect matching for candidate graph");
      }
      for (final DefaultWeightedEdge matchEdge : matching) {
         final PassengerExit matchedExit = (PassengerExit) graph.getEdgeTarget(matchEdge);
         final PassengerEntry matchedEntry = (PassengerEntry) graph.getEdgeSource(matchEdge);
         weightChanges[matchedExit.getExchangeIndex()] -= passengerWeights[matchedEntry.getIndex()];
         if (matchedExit.hasReachedDestination()) {
            solutionBuilder.completedTrip(
               CompletedTrip.builder()
                  .index(matchedEntry.getIndex())
                  .originFloor(matchedEntry.getOriginFloor())
                  .destinationFloor(matchedExit.getDestinationFloor())
                  .weight(passengerWeights[matchedEntry.getIndex()])
                  .build());
         } else {
            solutionBuilder.ongoingTraveller(
               OngoingTraveller.builder()
                  .index(matchedEntry.getIndex())
                  .originFloor(matchedEntry.getOriginFloor())
                  .weight(passengerWeights[matchedEntry.getIndex()])
                  .build());
         }
         // log.info(
         // String.format("%s matched with %s, weighing %f", matchedEntry.getLabel(), matchedExit.getLabel(),
         // passengerWeights[matchedEntry.getIndex()]));
      }

      double fitness = 0.0;
      for (int ii = 0; ii < exchangeCount; ii++) {
         final PassengerExchange nextExchange = exchanges[ii];
         if (nextExchange != null) {
            final double actualChange = weightChanges[ii];
            final double partialScore = nextExchange.getPartialScore(actualChange);
            fitness += partialScore;
            solutionBuilder.scoreComponent(
               ScoreComponent.builder()
                  .partialScore(partialScore)
                  .actualWeightChange(actualChange)
                  .expectedWeightChange(nextExchange.getExpectedWeightChange())
                  .build());
         }
      }
      solutionBuilder.fitness(fitness);
       log.info("Calculated fitness score of {}", fitness);

      return solutionBuilder.build();
   }
}
