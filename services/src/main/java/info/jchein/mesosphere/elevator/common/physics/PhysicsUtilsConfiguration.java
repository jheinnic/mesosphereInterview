package info.jchein.mesosphere.elevator.common.physics;


import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentConfiguration;


@Configuration
public class PhysicsUtilsConfiguration
{
   private final DeploymentConfiguration deploymentConfig;

   @Autowired
   public PhysicsUtilsConfiguration(@NotNull DeploymentConfiguration deploymentConfig) {
      this.deploymentConfig = deploymentConfig;
   }

   @Bean("physicsService")
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public IElevatorPhysicsService physicsService()
   {
      return new ElevatorPhysicsService(deploymentConfig) {
         protected IBrakingSolver getBrakingSolver(final double brakeDistance, final double brakeVelocity, final double initialVelocity, final double maxJerk) {
            return PhysicsUtilsConfiguration.this.brakingSolver(brakeDistance, brakeVelocity, initialVelocity, maxJerk);
         }
      };
   }


   @Bean("brakingSolver")
   @Scope(BeanDefinition.SCOPE_PROTOTYPE)
   public IBrakingSolver brakingSolver(final double brakeDistance, final double brakeVelocity,
      final double initialVelocity, final double maxJerk)
   {
      return new BrakingSolver(brakeDistance, brakeVelocity, initialVelocity, maxJerk);
   }
}
