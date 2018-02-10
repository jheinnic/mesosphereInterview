package info.jchein.mesosphere.elevator.simulator.model;

import java.util.Properties;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.simulator.passengers.ISimulatedTravellerSourceLookup;
import info.jchein.mesosphere.elevator.simulator.passengers.TravellerSourceNames;

@Configuration
@ComponentScan({"info.jchein.mesosphere.elevator.simulator.workloads", "info.jchein.mesosphere.elevator.simulator.model"})
public class ElevatorSimulationConfiguration {
   @Bean
   @Lazy
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public ServiceLocatorFactoryBean simulatedTravellerSourceLocator(TravellerSourceNames sourceNames) {
      final Properties serviceMappings = new Properties();
      sourceNames.getSourceNames().stream().forEach( namePrefix -> {
         serviceMappings.put( namePrefix, namePrefix + "TravellerSource" );
      });

      final ServiceLocatorFactoryBean slFactory = new ServiceLocatorFactoryBean();
      slFactory.setServiceLocatorInterface(ISimulatedTravellerSourceLookup.class);
      slFactory.setServiceMappings(serviceMappings);
      
      return slFactory;
   }

}
