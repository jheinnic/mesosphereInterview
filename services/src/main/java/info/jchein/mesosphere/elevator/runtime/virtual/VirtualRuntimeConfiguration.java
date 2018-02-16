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

import info.jchein.mesosphere.elevator.common.bootstrap.VirtualRuntimeProperties;
import info.jchein.mesosphere.elevator.runtime.IRuntime;
import rx.Scheduler;
import rx.Scheduler.Worker;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

@Configuration
@ComponentScan("info.jchein.mesosphere.elevator.runtime.virtual")
@EnableConfigurationProperties(VirtualRuntimeProperties.class)
public class VirtualRuntimeConfiguration {
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   @Qualifier(IRuntime.ELEVATOR_RUNTIME_QUALIFIER)
   Worker getRuntimeWorker(@Qualifier(IRuntime.ELEVATOR_RUNTIME_QUALIFIER) Scheduler scheduler) {
      return scheduler.createWorker();
   }
   
   
   @Bean
   @Profile("elevator.runtime.virtual")
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   @Qualifier(IRuntime.ELEVATOR_RUNTIME_QUALIFIER)
   TestScheduler getVirtualScheduler() {
      return Schedulers.test();
   }


   @Bean
   @Profile("elevator.runtime.live")
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   @Qualifier(IRuntime.ELEVATOR_RUNTIME_QUALIFIER)
   Scheduler getSystemScheduler() {
      return Schedulers.from(
         Executors.newSingleThreadExecutor());
   }
}