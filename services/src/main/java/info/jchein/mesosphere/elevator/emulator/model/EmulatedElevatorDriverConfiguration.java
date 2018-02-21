package info.jchein.mesosphere.elevator.emulator.model;


import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.common.bootstrap.InitialCarState;
import info.jchein.mesosphere.elevator.common.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarPort;


@Configuration
@ComponentScan("info.jchein.mesosphere.elevator.emulator.model")
public class EmulatedElevatorDriverConfiguration
{
   public static final String QUALIFIER_PREFIX = "mesosphere.elevator.emulator";

   public static final String CAR_DRIVER_FACTORY_BEAN_NAME =
      QUALIFIER_PREFIX + ".emulatedCarDriverFactory";


//   @Bean
//   @Scope(BeanDefinition.SCOPE_PROTOTYPE)
//   EmulatedElevatorCar emulatedElevatorCar(ElevatorEmulator elevator, IElevatorCarPort port, IElevatorPhysicsService physicsService, InitialCarState data)
//   {
//      return new EmulatedElevatorCar(elevator, port, physicsService, data);
//   }
}
