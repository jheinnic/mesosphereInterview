package info.jchein.mesosphere.elevator.common.bootstrap;


import java.util.Collection;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.statefulj.framework.core.StatefulFactory;

import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentConfiguration;
import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentProperties;
import info.jchein.mesosphere.elevator.common.bootstrap.IConfigurationFactory;
import info.jchein.mesosphere.elevator.control.sdk.ExtensionDescriptor;
import info.jchein.mesosphere.elevator.control.sdk.ExtensionType;


@Configuration
@ComponentScan({
   "info.jchein.mesosphere.elevator.common.bootstrap"
})
@EnableConfigurationProperties({
   DemographicProperties.class, DeploymentProperties.class,
   EmulatorProperties.class, VirtualRuntimeProperties.class
})
public class BootstrapConfiguration
{
   @Bean
   @Autowired
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public DemographicConfiguration
   demographicConfiguration(DemographicProperties mutableProps, IConfigurationFactory configFactory)
   {
      return configFactory.hardenDemographicConfig(mutableProps);
   }

   @Bean
   @Autowired
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public DeploymentConfiguration
   deploymentConfiguration(DeploymentProperties mutableProps, IConfigurationFactory configFactory)
   {
      return configFactory.hardenDeploymentConfig(mutableProps);
   }

   @Bean
   @Autowired
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public EmulatorConfiguration
   emulatorConfiguration(EmulatorProperties mutableProps, IConfigurationFactory configFactory)
   {
      return configFactory.hardenEmulatorConfig(mutableProps);
   }
   @Bean
   @Autowired
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public VirtualRuntimeDescription
   virtualRuntimeDescription(VirtualRuntimeProperties mutableProps, IConfigurationFactory configFactory)
   {
      return configFactory.hardenVirtualRuntimeConfig(mutableProps);
   }
}
