package info.jchein.mesosphere.elevator.simulator.passengers;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.simulator.model.ISimulatedPopulation;


@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SimulationEcosystem
{
   private final List<ISimulatedPopulation> populations;
   private final IRuntimeScheduler scheduler;
   private final IRuntimeClock clock;
   private final IRuntimeEventBus eventBus;


   @Autowired
   public SimulationEcosystem( List<ISimulatedPopulation> populations, IRuntimeClock clock,
      IRuntimeScheduler scheduler, IRuntimeEventBus eventBus )
   {
      this.populations = populations;
      this.clock = clock;
      this.scheduler = scheduler;
      this.eventBus = eventBus;
   }


   public void start()
   {
      this.populations.forEach(
         nextPopulation -> nextPopulation.initPoissonProcess(
            this.clock, this.scheduler, this.eventBus));
   }
}
