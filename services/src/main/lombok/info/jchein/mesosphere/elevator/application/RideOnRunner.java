package info.jchein.mesosphere.elevator.application;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.simulator.model.SimulationEcosystem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class RideOnRunner
implements ApplicationRunner
{
   private IRuntimeClock clock;
   private IRuntimeScheduler scheduler;
   private SimulationEcosystem simulationEcosystem;


   @Autowired
   public RideOnRunner( IRuntimeScheduler scheduler, IRuntimeClock clock, SimulationEcosystem simulationEcosystem )
   {
      this.clock = clock;
      this.scheduler = scheduler;
      this.simulationEcosystem = simulationEcosystem;
   }
   
   
   @Override
   public void run(ApplicationArguments args) throws Exception
   {
       this.simulationEcosystem.start();
       this.scheduler.begin();
       this.clock.advanceBy(5, TimeUnit.MINUTES);
       log.info("First five minutes passed by");
       this.clock.advanceBy(5, TimeUnit.MINUTES);
       log.info("Second five minutes passed by");
       this.clock.advanceBy(5, TimeUnit.MINUTES);
       log.info("Third five minutes passed by");
       this.clock.advanceBy(5, TimeUnit.MINUTES);
       log.info("Fourth five minutes passed by");
       this.clock.advanceBy(5, TimeUnit.MINUTES);
       log.info("Fifth five minutes passed by");
       this.clock.advanceBy(5, TimeUnit.MINUTES);
       log.info("Half an hour has now passed by");
   }
}
