package info.jchein.mesosphere.elevator.simulator.workloads;


import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.common.probability.IDistributionFactory;
import info.jchein.mesosphere.elevator.common.probability.IPopulationSampler;
import info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return.PopulationFactoryWithLobbyReturn;
import info.jchein.mesosphere.elevator.simulator.passengers.with_lobby_return.WithLobbyReturnRandomVariableSupplier;


@Configuration
@Profile({ "workload.toy" })
public class ToyTrafficPatternConfiguration
{
   private Supplier<PassengerId> idFactory;
   private IDistributionFactory distFactory;
   private IPopulationSampler weightSampler;


   @Autowired
   public ToyTrafficPatternConfiguration( Supplier<PassengerId> idFactory, IDistributionFactory distFactory,
      @Qualifier("weight") IPopulationSampler weightSampler)
   {
      this.idFactory = idFactory;
      this.distFactory = distFactory;
      this.weightSampler = weightSampler;
   }


   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   PopulationFactoryWithLobbyReturn populationOne()
   {
      final WithLobbyReturnRandomVariableSupplier rndSrc = new WithLobbyReturnRandomVariableSupplier(
         this.weightSampler, 5, this.distFactory.createNormalDist(1800, 120)
      );
      return new PopulationFactoryWithLobbyReturn("populationOne", this.idFactory, rndSrc, this.distFactory.createExponentialDist(25));
   }


   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   PopulationFactoryWithLobbyReturn populationTwo()
   {
      final WithLobbyReturnRandomVariableSupplier rndSrc = new WithLobbyReturnRandomVariableSupplier(
         this.weightSampler, 4, this.distFactory.createNormalDist(3600, 600)
      );
      return new PopulationFactoryWithLobbyReturn("populationTwo", this.idFactory, rndSrc, this.distFactory.createExponentialDist(120));
   }
}
