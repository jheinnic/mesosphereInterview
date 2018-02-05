package info.jchein.mesosphere.elevator.control.model;

import java.util.Collection;
import java.util.Properties;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.statefulj.framework.core.StatefulFactory;

import info.jchein.mesosphere.elevator.common.bootstrap.ElevatorGroupBootstrap;
import info.jchein.mesosphere.elevator.control.sdk.ExtensionDescriptor;
import info.jchein.mesosphere.elevator.control.sdk.ExtensionType;

@Configuration
@ComponentScan
@EnableConfigurationProperties(ElevatorGroupBootstrap.class)
public class ElevatorControllerConfiguration {
   @Bean
   public StatefulFactory statefulJFactory() {
      return new StatefulFactory();
   }
   
   @Bean
   @Lazy
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public ServiceLocatorFactoryBean elevatorCarDriverLocator(Collection<ExtensionDescriptor> registeredExtensions) {
      final Properties serviceMappings = new Properties();
      registeredExtensions.stream().filter(
         extension -> extension.extensionType == ExtensionType.ELEVATOR_CAR_DRIVER
      ).forEach( descriptor -> {
         serviceMappings.put( descriptor.getLookupKey(), descriptor.getBeanName());
      });

      final ServiceLocatorFactoryBean slFactory = new ServiceLocatorFactoryBean();
      slFactory.setServiceLocatorInterface(IElevatorCarDriverFactoryLocator.class);
      slFactory.setServiceMappings(serviceMappings);
      
      return slFactory;
   }
}