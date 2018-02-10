package info.jchein.mesosphere.elevator.simulator.workloads;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.simulator.passengers.TravellerSourceNames;
import info.jchein.mesosphere.elevator.simulator.model.ITravellerQueueService;

@Configuration
@Profile({ "workload.morning" })
public class MorningTrafficPatternConfiguration {
   private IRuntimeClock systemClock;
   private IRuntimeScheduler scheduler;	
	private ITravellerQueueService arrivalStrategy;

	@Autowired
	public MorningTrafficPatternConfiguration(
		IRuntimeClock systemClock, IRuntimeScheduler scheduler, ITravellerQueueService arrivalStrategy)
	{
		this.systemClock = systemClock;
      this.scheduler = scheduler;
		this.arrivalStrategy = arrivalStrategy;
	}
	
   @Bean
   @Scope(BeanDefinition.SCOPE_PROTOTYPE)
   TravellerSourceNames travellerSourceNames() {
      return TravellerSourceNames.build(bldr -> {
         bldr.sourceName("stuff")
            .sourceName("thing");
      });
   }
}
