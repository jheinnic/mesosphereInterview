package info.jchein.mesosphere.elevator.application;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.simulator.passengers.SimulationEcosystem;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RideOnRunner
implements ApplicationRunner
{
   private IRuntimeScheduler scheduler;
   private IRuntimeClock clock;
   private SimulationEcosystem simulationEcosystem;


   @Autowired
   public RideOnRunner( IRuntimeScheduler scheduler, IRuntimeClock clock, SimulationEcosystem simulationEcosystem )
   {
      this.scheduler = scheduler;
      this.clock = clock;
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
