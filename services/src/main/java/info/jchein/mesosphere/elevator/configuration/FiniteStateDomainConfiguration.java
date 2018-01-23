package info.jchein.mesosphere.elevator.configuration;

import org.springframework.context.annotation.Bean;
import org.statefulj.framework.core.StatefulFactory;
import org.statefulj.fsm.Persister;

import info.jchein.mesosphere.elevator.simulator.passengers.TravelPathFSM;

public class FiniteStateDomainConfiguration {
	@Bean
	public StatefulFactory statefulJFactory() {
		return new StatefulFactory();
	}
}
