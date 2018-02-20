package test;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.Subscribe;

import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.simulator.event.TravellerEvent;
import info.jchein.mesosphere.elevator.simulator.passengers.SimulationEcosystem;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class RunSourceRunner
implements ApplicationRunner
{
   private final IRuntimeScheduler scheduler;
   private final IRuntimeClock clock;
   private final IRuntimeEventBus eventBus;
   private final SimulationEcosystem ecosystem;


   @Autowired
   public RunSourceRunner( IRuntimeScheduler scheduler, IRuntimeClock clock, IRuntimeEventBus eventBus, SimulationEcosystem ecosystem ) {
      this.scheduler = scheduler;
      this.clock = clock;
      this.eventBus = eventBus;
      this.ecosystem = ecosystem;
      
   }
   
   
   @Override
   public void run(ApplicationArguments args) throws Exception
   {
      this.eventBus.registerListener(this);
      this.ecosystem.start();
      this.scheduler.begin();
      this.clock.advanceBy(7200, TimeUnit.SECONDS);
   }
   
   
   @Subscribe
   public void acceptEvent(TravellerEvent event) {
      System.out.println(String.format("At %d: [%s]", this.clock.now(), event.toString()));
   }

}
