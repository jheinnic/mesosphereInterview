package info.jchein.mesosphere.elevator.simulator.workloads;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.simulator.passengers.TravellerSourceNames;

@Configuration
@Profile({ "workload.morning" })
@EnableConfigurationProperties(MorningTrafficPatternProperties.class)
public class MorningTrafficPatternConfiguration {
   private IRuntimeClock systemClock;
   private IRuntimeScheduler scheduler;	

	@Autowired
	public MorningTrafficPatternConfiguration(IRuntimeClock systemClock, IRuntimeScheduler scheduler)
	{
		this.systemClock = systemClock;
      this.scheduler = scheduler;
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
