package test.jcop2;


import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.common.probability.IPopulationSampler;


@Configuration
public class WhoGoesWhereConfiguration
{
   private static final int SCORE_SAMPLE_COUNT = 1000;

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
      final double[] weightSamples = new double[SCORE_SAMPLE_COUNT];
      final StandardDeviation stdDev = new StandardDeviation();

      return new IWhoGoesWhereFactory() {
         @Override
         public WhoGoesWhere allocateProblem(String id, Building building)
         {
            return new WhoGoesWhere(id, building) {
               final WhoGoesWhereFitness fitness =
                  new WhoGoesWhereFitness(this, WhoGoesWhereConfiguration.this.populationSampler) {

                     @Override
                     protected Gaussian getScoreFunction(double weightDelta, int passengersIn, int passengersOut)
                     {
                        for (int jj = 0; jj < SCORE_SAMPLE_COUNT; jj++) {
                           double nextSample = 0;
                           for (int kk = 0; kk < passengersIn; kk++) {
                              nextSample += populationSampler.sample();
                           }
                           for (int kk = 0; kk < passengersOut; kk++) {
                              nextSample -= populationSampler.sample();
                           }
                           weightSamples[jj] = nextSample;
                        }
                        return new Gaussian(
                           1.0, weightDelta, stdDev.evaluate(weightSamples, weightDelta));
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
