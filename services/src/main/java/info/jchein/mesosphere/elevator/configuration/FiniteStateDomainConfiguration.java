package info.jchein.mesosphere.elevator.configuration;

import org.springframework.context.annotation.Bean;
import org.statefulj.framework.core.StatefulFactory;

public class FiniteStateDomainConfiguration {
	@Bean
	public StatefulFactory statefulJFactory() {
		return new StatefulFactory();
	}
}
