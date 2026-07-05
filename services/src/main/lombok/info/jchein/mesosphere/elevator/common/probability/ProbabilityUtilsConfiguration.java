package info.jchein.mesosphere.elevator.common.probability;


import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.common.bootstrap.DemographicConfiguration;
import info.jchein.mesosphere.elevator.common.demographic.AgeGroupWeightSample;


@Configuration
@ComponentScan("info.jchein.mesosphere.elevator.common.probability")
public class ProbabilityUtilsConfiguration
{
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   @Qualifier(IDistributionFactory.RANDOM_SOURCE_QUALIFIER)
   RandomGenerator distributionRandomGenerator()
   {
      return new MersenneTwister();
   }


   @Bean
   @Autowired
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   @Qualifier("weight")
   IPopulationSampler passengerWeightSampler(IDistributionFactory distFactory, DemographicConfiguration demoConfig)
   {
      return distFactory.createPopulationSampler(
         ImmutableList.of(demoConfig.getFemaleWeightSamples(), demoConfig.getMaleWeightSamples())
            .stream()
            .<AgeGroupWeightSample> flatMap(ImmutableList::stream)::iterator,
         (ageGroup) -> Double.valueOf(ageGroup.getCount()),
         (ageGroup) -> {
            return distFactory.createNormalDist(ageGroup.getWeightMean(), ageGroup.getWeightStdDev());
         });
   }
}
