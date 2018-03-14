package test.sampling;


import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import info.jchein.mesosphere.elevator.common.probability.IPopulationSampler;


//@Component
//@Scope(BeanDefinition.SCOPE_SINGLETON)
public class RunSampleWeightsRunner
implements ApplicationRunner
{
   private final IPopulationSampler populationSampler;


   @Autowired
   RunSampleWeightsRunner( IPopulationSampler populationSampler )
   {
      this.populationSampler = populationSampler;
   }


   @Override
   public void run(ApplicationArguments args) throws Exception
   {
      for (int ii = 0; ii < 30; ii++) {
         final double value = this.populationSampler.sample();
         System.out.println(String.format("#%d: %f", (ii + 1), value));
      }

      for (double ii = 0.10; ii < 1; ii += .05) {
         for (double jj = 0.10; jj < 1; jj += .05) {
            System.out.println(
               String
                  .format("P(G) <= %f;  P(%f|G) = %f", ii, this.populationSampler.lookup(ii, jj), jj));
         }
      }

      StandardDeviation stdDev = new StandardDeviation();
      for (int ii = 3; ii < 50; ii += 5) {
         for (int jj = 500; jj < 5000; jj += 1000) {
            double sum = 0;
            final double[] samples = new double[jj];
            for (int kk = 0; kk < jj; kk++) {
               double localSum = 0;
               for (int ll = 0; ll < ii; ll++) {
                  localSum += this.populationSampler.sample();
               }
               samples[kk] = localSum;
               sum += localSum;
            }
            System.out.println(
               String.format(
                  "For %d samples of %d, total=%f, avg=%f, stddev=%f, stddev per item=%f",
                  jj,
                  ii,
                  sum,
                  sum / jj,
                  stdDev.evaluate(samples),
                  stdDev.evaluate(samples)/ii));
         }
      }
   }
}
