package info.jchein.mesosphere.elevator.simulator.passengers;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.event.RuntimeEventBus;
import info.jchein.mesosphere.elevator.simulator.model.IElevatorSimulation;
import info.jchein.mesosphere.elevator.simulator.model.ISimulatedPopulation;


@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SimulationEcosystem // <V extends ITravellerRandomVariables, T extends AbstractSimulatedTraveller<V>>
                                 // implements ISimulatedTravellerSource
{

   private final List<ISimulatedPopulation> populations;
   private IRuntimeScheduler runtimeScheduler;


   @Autowired
   public SimulationEcosystem( List<ISimulatedPopulation> populations,
      @Qualifier(IElevatorSimulation.TRAVELLER_SOURCE_QUALIFIER) IRuntimeScheduler runtimeScheduler )
   {
      this.populations = populations;
      this.runtimeScheduler = runtimeScheduler;
   }


   public void start()
   {

   }
}
