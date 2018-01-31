package info.jchein.mesosphere.elevator.domain.emulator;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

@Configuration
@Profile("elevator.runtime.virtual")
public class VirtualElevatorEmulatorConfiguration
{
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
}
