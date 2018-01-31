package info.jchein.mesosphere.elevator.runtime;

import java.time.ZoneId;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.elevator.domain.common.ElevatorGroupBootstrap;
import info.jchein.mesosphere.elevator.domain.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorDispatcherPort;
import info.jchein.mesosphere.elevator.scheduler.tracking.HeuristicElevatorSchedulingStrategy;
import rx.Scheduler;
import rx.Scheduler.Worker;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

@Configuration
public class RuntimeFrameworkConfiguration {
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   @Qualifier("mesosphere.elevator.control.runtime")
   public EventBus getGuavaEventBus() {
      return new EventBus();
   }
   
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public IRuntimeEventBus getRuntimeEventBus(@Qualifier("mesosphere.elevator.control.runtime") EventBus guavaEventBus) {
      return new RuntimeEventBus(guavaEventBus);
   }
   
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public IRuntimeClock getRuntimeClock(@NotNull @Qualifier("mesosphere.elevator.control.runtime") Scheduler rxScheduler, @NotNull SystemRuntimeProperties runtimeProps)
   {
      return new RuntimeClock(ZoneId.systemDefault(), rxScheduler, runtimeProps);
   }
   
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public IRuntimeScheduler getRuntimeScheduler(@NotNull @Qualifier("mesosphere.elevator.control.runtime") Scheduler rxScheduler, @NotNull SystemRuntimeProperties runtimeProps)
   {
      return new RuntimeScheduler(rxScheduler, runtimeProps);
   }
   
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
// public RuntimeService getSystemClock(Scheduler scheduler, SystemRuntimeProperties runtimeProperties) {
   public IRuntimeService getRuntimeService(RuntimeService service)
   {
      return service;
//    return new RuntimeService(runtimeProperties, scheduler);
   }

   @Bean
   @Profile("elevator.runtime.virtual")
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   @Qualifier("mesosphere.elevator.control.runtime")
   TestScheduler getScheduler() {
      return Schedulers.test();
   }
}