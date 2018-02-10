package info.jchein.mesosphere.elevator.simulator.passengers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.simulator.model.ISimulatedTravellerSource;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SimulatedTravellerGenerator // <V extends ITravellerRandomVariables, T extends AbstractSimulatedTraveller<V>> implements ISimulatedTravellerSource
{
   private final ImmutableList<ISimulatedTravellerSource> travellerSources;

   @Autowired
   SimulatedTravellerGenerator(ISimulatedTravellerSourceLookup sourceLookup, TravellerSourceNames sourceBeanNames)
   {
      ImmutableList.Builder<ISimulatedTravellerSource> sourceBuilder = ImmutableList.builder();
      sourceBeanNames.getSourceNames().forEach( beanName -> {
         sourceBuilder.add(
            sourceLookup.getTravellerSource(beanName));
      });
      this.travellerSources = sourceBuilder.build();
   }
}
