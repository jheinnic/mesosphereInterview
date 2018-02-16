package info.jchein.mesosphere.elevator.common.physics;

import java.util.List;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.util.Pair;

import com.google.common.base.Function;

public interface IDistributionFactory
{   
   public static final String RANDOM_SOURCE_QUALIFIER = "mesosphere.physics.distributions.random";
   
   NormalDistribution createNormalDist(double mean, double stdDev);

   NormalDistribution createNormalDist(double mean, double stdDev, double inverseCumAccuracy);
   
   LogNormalDistribution createLogNormalDist(double scale, double sigma);

   LogNormalDistribution createLogNormalDist(double scale, double sigma, double inverseCumAccuracy);

   ExponentialDistribution createExponentialDist(double mean);

   ExponentialDistribution createExponentialDist(double mean, double inverseCumAccuracy);
   
   EnumeratedIntegerDistribution createEnumeratedIntegerDist(int[] values, double[] propbabilities);
   
   <T> EnumeratedDistribution<T> createEnumeratedDist(Iterable<Pair<T, Double>> content);

   <T> EnumeratedDistribution<T> createEnumeratedDist(Iterable<T> items, Function<T, Double> probFn);

   <I, T extends RealDistribution> IPopulationSampler createPopulationSampler(
      Iterable<I> items, Function<I, Double> probFn, Function<I, T> realDistFn);

   <I, T extends RealDistribution> IPopulationSampler createPopulationSampler(
      Iterable<I> items, Function<I, Pair<T, Double>> mapFn );

   <T extends RealDistribution> IPopulationSampler createPopulationSampler(
      Iterable<Pair<T, Double>> items);
}

