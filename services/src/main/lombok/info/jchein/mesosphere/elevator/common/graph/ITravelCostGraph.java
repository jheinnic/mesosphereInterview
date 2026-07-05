package info.jchein.mesosphere.elevator.common.graph;

import java.util.List;

public interface ITravelCostGraph<V>
{
   public double getTraversalCost(V from, V to);
   
   public double getPathTraversalCost(List<V> firstToLast);
   
   public interface ICostFunction {
      double apply(int fromIndex, int toIndex);
   }
}
