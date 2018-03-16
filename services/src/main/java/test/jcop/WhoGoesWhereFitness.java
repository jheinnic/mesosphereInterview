package test.jcop;


import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.MathArrays;
import org.jgrapht.alg.interfaces.MatchingAlgorithm.Matching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.builder.GraphBuilder;

import cz.cvut.felk.cig.jcop.problem.BaseFitness;
import cz.cvut.felk.cig.jcop.problem.Configuration;
import cz.cvut.felk.cig.jcop.problem.Fitness;
import cz.cvut.felk.cig.jcop.util.JcopRandom;
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
   private SimpleWeightedGraph<PassengerVertex, DefaultWeightedEdge> matchingGraph;
   private DefaultWeightedEdge[][] entryExitEdges;

   private Set<PassengerVertex> arrivalSet;
   private Set<PassengerVertex> departureSet;


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

      // This populates matchingGraph and entryExitEdges
      this.constructMatchingGraph(problem);

      // Prepackage the arrivals and departure vertices in Sets, since thats how the matching algorithms expect to 
      // be provided their values.
      this.arrivalSet =
         Arrays.<PassengerVertex> stream(problem.getArrivals())
            .collect(Collectors.toSet());
      this.departureSet =
         Arrays.<PassengerVertex> stream(problem.getDepartures())
            .collect(Collectors.toSet());
   }

   protected abstract
   GraphBuilder<PassengerVertex, DefaultWeightedEdge, SimpleWeightedGraph<PassengerVertex, DefaultWeightedEdge>>
   getGraphBuilder();

   protected abstract Matching<PassengerVertex, DefaultWeightedEdge>
   getOptimalMatching(SimpleWeightedGraph<PassengerVertex, DefaultWeightedEdge> graph,
      Set<PassengerVertex> incomingSet, Set<PassengerVertex> outgoingSet);


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


   private void constructMatchingGraph(IWhoGoesWhereProblem problem)
   {
      final GraphBuilder<PassengerVertex, DefaultWeightedEdge, SimpleWeightedGraph<PassengerVertex, DefaultWeightedEdge>> graphBuilder =
         this.getGraphBuilder();

      final PassengerEntry[] arrivals = problem.getArrivals();
      final PassengerExit[] departures = problem.getDepartures();
      graphBuilder.addVertices(arrivals)
         .addVertices(departures);

      final int passengerCount = arrivals.length;
      this.entryExitEdges = new DefaultWeightedEdge[passengerCount][];
      for (int ii = 0; ii < passengerCount; ii++) {
         // Read the static problem sizing indices stored with the next passenger pickup entry and allocate edges to
         // hold implied by a given Configuration while testing its fitness.
         final PassengerEntry nextEntry = arrivals[ii];
         final int firstOutboundIndex = nextEntry.getFirstOutboundIndex();
         final int matchCandidateCount = nextEntry.getMatchCandidateCount();
         this.entryExitEdges[ii] = new DefaultWeightedEdge[matchCandidateCount];

         for (int jj = 0, kk = firstOutboundIndex; jj < matchCandidateCount; jj++, kk++) {
            this.entryExitEdges[ii][jj] = new DefaultWeightedEdge();
            graphBuilder.addEdge(nextEntry, departures[kk], this.entryExitEdges[ii][jj]);
         }
      }

      this.matchingGraph = graphBuilder.build();
   }


   public CandidateSolution transformConfiguration(Configuration configuration)
   {
      final CandidateSolutionBuilder solutionBuilder = CandidateSolution.builder();
      solutionBuilder.configuration(configuration);

      final PassengerEntry[] arrivals = this.problem.getArrivals();
      final PassengerExchange[] exchanges = this.problem.getExchanges();

      // Store passenger weights orthogonally to the passenger vertices to facilitate concurrent reuse.
      final int passengerCount = arrivals.length;
      final double[] passengerWeights = new double[passengerCount];

      // Store mass values from Configuration orthogonally to the passenger vertices to facilitate concurrent reuse.
      final int exchangeCount = exchanges.length;
      final double[] weightChanges = new double[exchangeCount];

      // Store match bias edge weights from Configuration one source at a time--once they are assigned to graph,
      // we can recycle.
      int lastMatchCandidateCount = -1;
      double[] matchWeights = null;

      for (int ii = 0; ii < passengerCount; ii++) {
         // Get the iith passenger entry vertex and its list of candidate edges for weighting.
         final PassengerEntry nextEntry = arrivals[ii];
         final DefaultWeightedEdge[] matchCandidates = this.entryExitEdges[ii];

         // Read the static problem sizing indices stored with the next passenger pickup entry.
         final int weightIndex = ii * 4; // nextEntry.getWeightIndex();
         final int exchangeIndex = nextEntry.getExchangeIndex();
         final int matchCandidateCount = nextEntry.getMatchCandidateCount();

         // Try to recycle the array for normalizing match randomization weights.
         if (matchCandidateCount != lastMatchCandidateCount) {
            matchWeights = new double[matchCandidateCount];
         }

         // Read the sampling key used to embed the weight for current passenger in configuration's proposed solution.
         final double attrValueOne = configuration.valueAt(weightIndex) * ATTR_SCALE;
         final double attrValueTwo = configuration.valueAt(weightIndex + 1) * ATTR_SCALE;
         passengerWeights[ii] = this.populationSampler.lookup(attrValueOne, attrValueTwo);
         weightChanges[exchangeIndex] += passengerWeights[ii];

         // Next two integer parameters provide a seed for generating vertex matching edge weights.
         final int lowSeed = configuration.valueAt(weightIndex + 2);
         final int highSeed = configuration.valueAt(weightIndex + 3);
         final long seedAttr = (((long) highSeed) << 32) | (lowSeed & 0xffffffffL);
         JcopRandom.setSeed(seedAttr);

         // Convert the seed attribute to a run of unnormalized edge weights. This is deterministic.
         for (int jj = 0; jj < matchCandidateCount; jj++) {
            matchWeights[jj] = JcopRandom.nextDouble();
         }

         // Normalize the edge weights to 1000.
         matchWeights = MathArrays.normalizeArray(matchWeights, 1000.00);

         // Assign edge weights to edges
         for (int jj = 0; jj < matchCandidateCount; jj++) {
            this.matchingGraph.setEdgeWeight(matchCandidates[jj], matchWeights[jj]);
         }
      }

      // Run the matching
      final Matching<PassengerVertex, DefaultWeightedEdge> matching =
         this.getOptimalMatching(this.matchingGraph, this.arrivalSet, this.departureSet);
      if (!matching.isPerfect()) {
         log.warn("Fitness check computed imperfect matching for candidate graph");
      }
      
      // Process each edge found in the matching
      for (final DefaultWeightedEdge matchEdge : matching) {
         final PassengerExit matchedExit = (PassengerExit) this.matchingGraph.getEdgeTarget(matchEdge);
         final PassengerEntry matchedEntry = (PassengerEntry) this.matchingGraph.getEdgeSource(matchEdge);
         
         // Tally effect of outbound departures on weight flow, pairing the location of each departure with
         // the weight associated with its matched passenger arrival vertex, then normalize each exchange's
         // contribution to the overall score by applying the Gaussian function to the difference between
         // expected and actual weight change.  This function will equal 1 for a perfect match, and some
         // value between 0 and 1 for anything else, with smaller values associated with larger differences
         // between expected and actual.
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
      fitness = fitness / exchangeCount;
      solutionBuilder.fitness(fitness);
//      log.info("Calculated fitness ranking of {}", fitness);

      return solutionBuilder.build();
   }
}
