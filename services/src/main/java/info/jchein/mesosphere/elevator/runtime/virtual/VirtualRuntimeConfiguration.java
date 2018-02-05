package info.jchein.mesosphere.elevator.runtime.virtual;

import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.elevator.runtime.virtual.VirtualRuntimeProperties;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

@Configuration
//@ConfigurationProperties("mesosphere.elevator.runtime")
@EnableConfigurationProperties(VirtualRuntimeProperties.class)
@ComponentScan()
public class VirtualRuntimeConfiguration {
//   long tickDurationMillis = 200;
   
//   @Bean
//   @Scope(BeanDefinition.SCOPE_SINGLETON)
//   public SystemRuntimeProperties getFrameworkConfigProps() {
//      return SystemRuntimeProperties.build(bldr -> {bldr.tickDurationMillis(tickDurationMillis); });
//   }
   
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   @Qualifier(IVirtualRuntimeService.ELEVATOR_RUNTIME_QUALIFIER)
   public EventBus getGuavaEventBus() {
      return new EventBus();
   }
   
   /*
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
   */
   
   @Bean
   @Profile("elevator.runtime.virtual")
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   @Qualifier(IVirtualRuntimeService.ELEVATOR_RUNTIME_QUALIFIER)
   TestScheduler getVirtualScheduler() {
      return Schedulers.test();
   }

   @Bean
   @Profile("elevator.runtime.live")
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   @Qualifier(IVirtualRuntimeService.ELEVATOR_RUNTIME_QUALIFIER)
   Scheduler getSystemScheduler() {
      return Schedulers.from(
         Executors.newSingleThreadExecutor());
   }
}