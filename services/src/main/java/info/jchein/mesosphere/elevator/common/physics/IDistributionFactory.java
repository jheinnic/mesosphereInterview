package info.jchein.mesosphere.elevator.common.physics;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

public interface IDistributionFactory
{   
   NormalDistribution createNormalDist(double mean, double stdDev, double inverseCumAccuracy);
   
   LogNormalDistribution createLogNormalDist(double median);

   ExponentialDistribution createExponentialDist(double median, double inverseCumAccuracy);
   
   EnumeratedIntegerDistribution createEnumeratedInteger(int[] values, double[] propbabilities);
}
