package info.jchein.mesosphere.elevator.control.manifest;


import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphBuilder;

import info.jchein.mesosphere.elevator.common.graph.ITravelCostGraph;


public class PathCostGraph<V>
implements ITravelCostGraph<V>
{
   private DefaultDirectedWeightedGraph<V, DefaultEdge> costGraph;


   // =10-((N3)/(1.9*$M$2))^3.6
   PathCostGraph( Collection<V> vertices, BiFunction<V, V, Double> costFunction)
   {
      final GraphBuilder<V, DefaultEdge, DefaultDirectedWeightedGraph<V, DefaultEdge>> builder =
         new GraphBuilder<V, DefaultEdge, DefaultDirectedWeightedGraph<V, DefaultEdge>>(
            new DefaultDirectedWeightedGraph<V, DefaultEdge>(DefaultEdge.class));

      for (final V source : vertices) {
         for (final V target : vertices) {
            // Don't attemtpt to add edges between the identical vertices.
            if (source == target) {
               continue;
            }

            // Don't attempt to filter out inverse associations--presume the cost is at least defined in both
            // directions, if not necessarily asymmetric.
            final double cost = costFunction.apply(source, target);

            // This will implicitly add all vertices by time we're done, so no need to explicitly add anything but
            // edges.
            builder.addEdge(source, target, cost);
         }
      }

      this.costGraph = builder.build();
   }


   @Override
   @ParameterScriptAssert(lang = "javascript", script = "arg0 !== arg1")
   public double getTraversalCost(@NotNull V from, @NotNull V to)
   {
      return this.costGraph.getEdgeWeight(this.costGraph.getEdge(from, to));
   }


   @Override
   @SuppressWarnings("unchecked")
   public double getPathTraversalCost(@Size(min = 2) @NotNull List<V> firstToLast)
   {
      return (double) firstToLast.stream()
         .<Object[]> reduce((Object[]) null, (Object[] result, V nextVertex) -> {
            Object[] retVal;

            if (result == null) {
               retVal = new Object[] { 0, nextVertex, nextVertex };
            } else {
               // retVal[1] is the first node in series, used by the binary combine operator o collapse two adjacent
               // runs by their neighboring endpoints.
               // We don't use it here, just preserve it for when we do.
               retVal = new Object[] { ((Double) result[0]) + this.getTraversalCost((V) result[2], nextVertex), result[1], nextVertex };
            }

            return retVal;
         }, (Object[] left, Object[] right) -> {
            Object[] retVal;

            if (left == null) {
               if (right == null) {
                  retVal = null;
               } else {
                  retVal = right;
               }
            } else if (right == null) {
               retVal = left;
            } else {
               retVal =
                  new Object[]
                  {
                     ((Double) left[0]) + ((Double) right[0]) + this.getTraversalCost(
                        (V) left[2], (V) right[1]),
                     left[1], right[2]
               };
            }

            return retVal;
         })[0];
   }
}
