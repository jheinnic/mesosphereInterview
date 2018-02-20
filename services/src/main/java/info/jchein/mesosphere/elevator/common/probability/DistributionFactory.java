package info.jchein.mesosphere.elevator.common.physics;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;


@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class DistributionFactory
implements IDistributionFactory
{
   private RandomGenerator rng;


   /**
    * Establishes the source of randomness for all sequences produced by this class. It is presumed to have already been
    * seeded.
    * 
    * @param rng
    */
   @Autowired
   DistributionFactory( @Qualifier(IDistributionFactory.RANDOM_SOURCE_QUALIFIER) RandomGenerator rng )
   {
      this.rng = rng;
   }


   @Override
   public NormalDistribution createNormalDist(double mean, double stdDev)
   {
      return new NormalDistribution(this.rng, mean, stdDev);
   }


   @Override
   public NormalDistribution createNormalDist(double mean, double stdDev, double inverseCumAccuracy)
   {
      return new NormalDistribution(this.rng, mean, stdDev, inverseCumAccuracy);
   }


   @Override
   public LogNormalDistribution createLogNormalDist(double scale, double sigma)
   {
      return new LogNormalDistribution(this.rng, scale, sigma);
   }


   @Override
   public LogNormalDistribution
   createLogNormalDist(double scale, double sigma, double inverseCumAccuracy)
   {
      return new LogNormalDistribution(this.rng, scale, sigma, inverseCumAccuracy);
   }


   @Override
   public ExponentialDistribution createExponentialDist(double mean)
   {
      return new ExponentialDistribution(this.rng, mean);
   }


   @Override
   public ExponentialDistribution createExponentialDist(double mean, double inverseCumAccuracy)
   {
      return new ExponentialDistribution(this.rng, mean, inverseCumAccuracy);
   }


   @Override
   public EnumeratedIntegerDistribution
   createEnumeratedIntegerDist(int[] values, double[] probabilities)
   {
      return new EnumeratedIntegerDistribution(values, probabilities);
   }


   @Override
   public <T> EnumeratedDistribution<T> createEnumeratedDist(Iterable<Pair<T, Double>> content)
   {
      ArrayList<Pair<T, Double>> foo = new ArrayList<Pair<T, Double>>();
      content.iterator().forEachRemaining(foo::add);
      return new EnumeratedDistribution<>(foo);
   }


   @Override
   public <T> EnumeratedDistribution<T>
   createEnumeratedDist(Iterable<T> items, Function<T, Double> probFn)
   {
      final ImmutableList.Builder<Pair<T, Double>> listBuilder =
         ImmutableList.<Pair<T, Double>> builder();

      items.forEach(item -> {
         listBuilder.add(new Pair<T, Double>(item, probFn.apply(item)));
      });

      return new EnumeratedDistribution<>(listBuilder.build());
   }


   @Override
   public <I, T extends RealDistribution> IPopulationSampler
   createPopulationSampler(Iterable<I> items, Function<I, Double> probFn, Function<I, T> mapFn)
   {
      final EnumeratedDistribution<I> enumDist = this.createEnumeratedDist(items, probFn);
      return new IPopulationSampler() {
         @Override
         public double sample()
         {
            final I innerGroup = enumDist.sample();
            final T innerDist = mapFn.apply(innerGroup);
            return innerDist.sample();
         }
      };
   }


   @Override
   public <I, T extends RealDistribution> IPopulationSampler
   createPopulationSampler(Iterable<I> items, Function<I, Pair<T, Double>> mapFn)
   {
      final ImmutableList.Builder<Pair<T, Double>> listBuilder =
         ImmutableList.<Pair<T, Double>> builder();

      items.forEach(item -> {
         listBuilder.add(mapFn.apply(item));
      });

      final EnumeratedDistribution<T> enumDist =
         new EnumeratedDistribution<>(listBuilder.build());

      return new IPopulationSampler() {
         @Override
         public double sample()
         {
            final T innerDist = enumDist.sample();
            return innerDist.sample();
         }
      };
   }


   @Override
   public <T extends RealDistribution> IPopulationSampler
   createPopulationSampler(Iterable<Pair<T, Double>> items)
   {
      final EnumeratedDistribution<T> enumDist =
         this.createEnumeratedDist(items);

      return new IPopulationSampler() {
         @Override
         public double sample()
         {
            final T innerDist = enumDist.sample();
            return innerDist.sample();
         }
      };
   }
}
