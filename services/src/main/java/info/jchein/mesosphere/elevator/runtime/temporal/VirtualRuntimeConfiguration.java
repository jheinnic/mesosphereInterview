package info.jchein.mesosphere.elevator.runtime.temporal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.common.bootstrap.VirtualRuntimeDescription;
import info.jchein.mesosphere.elevator.runtime.IRuntime;
import rx.Scheduler;
import rx.Scheduler.Worker;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

@Configuration
@ComponentScan({"info.jchein.mesosphere.elevator.runtime.temporal"})
public class VirtualRuntimeConfiguration {
   @Bean
   @Autowired
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   @Qualifier(IRuntime.ELEVATOR_RUNTIME_QUALIFIER)
   Worker runtimeWorker(@Qualifier(IRuntime.ELEVATOR_RUNTIME_QUALIFIER) Scheduler scheduler) {
      return scheduler.createWorker();
   }
   
   
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   @Qualifier(IRuntime.ELEVATOR_RUNTIME_QUALIFIER)
   TestScheduler runtimeScheduler() {
      return Schedulers.test();
   }


   /*
   @Bean
   @Autowired
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public VirtualClock runtimeClock(@Qualifier(IRuntime.ELEVATOR_RUNTIME_QUALIFIER) Scheduler scheduler, VirtualRuntimeDescription runtimeDesc)
   {
      return new VirtualClock(scheduler, runtimeDesc);
   }
   */


   /*
   @Bean
   @Autowired
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   public VirtualRuntimeDescription virtualRuntimeDescription(VirtualRuntimeProperties mutableProperties, IConfigurationFactory configFactory)
   {
      return configFactory.hardenVirtualRuntimeConfig(mutableProperties);
   }
   */

   /*
   @Bean
   @Profile("elevator.runtime.live")
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   @Qualifier(IRuntime.ELEVATOR_RUNTIME_QUALIFIER)
   Scheduler getSystemScheduler() {
      return Schedulers.from(
         Executors.newSingleThreadExecutor());
   }
   */
}