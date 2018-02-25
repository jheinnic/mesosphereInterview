package info.jchein.mesosphere.elevator.emulator.model;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ServiceLocatorFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.control.IElevatorCarScope;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarPort;


@Configuration
@ComponentScan
public class EmulatorDriverConfiguration
{
//   public static final String QUALIFIER_PREFIX = "mesosphere.elevator.emulator";

   @Bean
   @Autowired
   @Scope(IElevatorCarScope.SCOPE_NAME)
   EmulatedElevatorCar emulatedElevatorCar(EmulatedElevator emulator, IElevatorCarPort port) 
   {
      return emulator.getCarEmulator(port);
   }
   
   
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public ServiceLocatorFactoryBean manifestUpdateSupplier()
   {
      final ServiceLocatorFactoryBean slFactory = new ServiceLocatorFactoryBean();
      slFactory.setServiceLocatorInterface(ManifestUpdateSupplier.class);
      return slFactory;
   }
 
}
