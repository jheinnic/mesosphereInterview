package info.jchein.mesosphere.elevator.common.physics;


import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PhysicsConfiguration
{
   @Bean
   @Autowired
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   IElevatorPhysicsService physicsService(DeploymentConfiguration config) {
      return new ElevatorPhysicsService(config);
   }

   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   IDistributionFactory distributionService() {
      return new DistributionFactory();
   }
}
