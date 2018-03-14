package info.jchein.mesosphere.elevator.common.probability;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
      return new EnumeratedIntegerDistribution(this.rng, values, probabilities);
   }


   @Override
   public <T> EnumeratedDistribution<T> createEnumeratedDist(List<Pair<T, Double>> content)
   {
      return new EnumeratedDistribution<>(this.rng, content);
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
   public <I> IPopulationSampler createPopulationSampler(Iterable<I> items, Function<I, Double> probFn,
      Function<I, RealDistribution> mapFn)
   {
      return this.createPopulationSampler(items, (item) -> {
         return new Pair<RealDistribution, Double>(mapFn.apply(item), probFn.apply(item));
      });
   }


   @Override
   public <I> IPopulationSampler
   createPopulationSampler(Iterable<I> items, Function<I, Pair<RealDistribution, Double>> mapFn)
   {
      return this.createPopulationSampler(
         StreamSupport.stream(items.spliterator(), false)
            .map(mapFn::apply)
            .collect(Collectors.toList()));
   }


   @Override
   public IPopulationSampler createPopulationSampler(List<Pair<RealDistribution, Double>> items)
   {
      final EnumeratedDistribution<RealDistribution> enumDist = this.createEnumeratedDist(items);
      return new TieredPopulationSampler(enumDist);
   }


   private class TieredPopulationSampler
   implements IPopulationSampler
   {
      private final EnumeratedDistribution<RealDistribution> enumDist;
      private double[] cumulativeProbabilities;
      private RealDistribution[] subgroupDists;


      private TieredPopulationSampler( EnumeratedDistribution<RealDistribution> enumDist )
      {
         this.enumDist = enumDist;

         // EnumeratedDistribution lacks an inverseCdf() method, so unfortunately we need to duplicate
         // some of its implementation in order to replace the random number generator with specific
         // inputs. Alternately, we could have build the same EnumeratedDistribution twice and injected
         // an owned implementation of RandomGenerator in order to feed specific inputs, but this would
         // have had an adverse effect on thread safety, so code duplication was utilized instead.
         final List<Pair<RealDistribution, Double>> probabilities = enumDist.getPmf();
         final int numGroups = probabilities.size();
         this.subgroupDists = new RealDistribution[numGroups];
         this.cumulativeProbabilities = new double[numGroups];

         double sum = 0;
         for (int i = 0; i < numGroups; i++) {
            final Pair<? extends RealDistribution, Double> pair = probabilities.get(i);
            sum += pair.getSecond();
            this.subgroupDists[i] = pair.getFirst();
            this.cumulativeProbabilities[i] = sum;
         }
      }


      @Override
      public double sample()
      {
         final RealDistribution innerDist = enumDist.sample();
         return innerDist.sample();
      }


      @Override
      public double lookup(double groupProb, double distProb)
      {
         final RealDistribution realDist = this.selectGroup(groupProb);
         return realDist.inverseCumulativeProbability(distProb);
      }


      private RealDistribution selectGroup(final double groupProb)
      {
         int index = Arrays.binarySearch(cumulativeProbabilities, groupProb);
         if (index < 0) {
            index = -index - 1;
         }

         if (index >= 0 &&
            index < this.cumulativeProbabilities.length &&
            groupProb < cumulativeProbabilities[index]) { return this.subgroupDists[index]; }

         /*
          * This should never happen, but it ensures we will return a correct object in case there is some floating
          * point inequality problem wrt the cumulative probabilities.
          */
         return this.subgroupDists[this.subgroupDists.length - 1];
      }
   }
}
