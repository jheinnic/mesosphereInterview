package test.jcop;


import java.util.Set;

import org.jgrapht.alg.interfaces.MatchingAlgorithm.Matching;
import org.jgrapht.alg.matching.MaximumWeightBipartiteMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.common.probability.IPopulationSampler;


@Configuration
public class WhoGoesWhereConfiguration
{
   private final IPopulationSampler populationSampler;

   @Autowired
   WhoGoesWhereConfiguration( IPopulationSampler populationSampler )
   {
      this.populationSampler = populationSampler;
   }


   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   protected IWhoGoesWhereFactory whoGoesWhereFactory()
   {
      return new IWhoGoesWhereFactory() {
         @Override
         public WhoGoesWhere allocateProblem(String id, Building building)
         {
            return new WhoGoesWhere(id, building) {
               final WhoGoesWhereFitness fitness =
                  new WhoGoesWhereFitness(this, WhoGoesWhereConfiguration.this.populationSampler) {
                     @Override
                     protected
                     GraphBuilder<PassengerVertex, DefaultWeightedEdge, SimpleWeightedGraph<PassengerVertex, DefaultWeightedEdge>>
                     getGraphBuilder()
                     {
                        return new GraphBuilder<PassengerVertex, DefaultWeightedEdge, SimpleWeightedGraph<PassengerVertex, DefaultWeightedEdge>>(
                           new SimpleWeightedGraph<PassengerVertex, DefaultWeightedEdge>(DefaultWeightedEdge.class));
                     }

                     @Override
                     protected Matching<PassengerVertex, DefaultWeightedEdge>
                     getOptimalMatching(
                        SimpleWeightedGraph<PassengerVertex, DefaultWeightedEdge> graph,
                        Set<PassengerVertex> arrivals,
                        Set<PassengerVertex> departures)
                     {
                        return new MaximumWeightBipartiteMatching<PassengerVertex, DefaultWeightedEdge>(graph, arrivals, departures)
                           .getMatching();
                     }
                  };

               @Override
               public WhoGoesWhereFitness getDefaultFitness()
               {
                  return this.fitness;
               }
            };
         }
      };
   }
}
