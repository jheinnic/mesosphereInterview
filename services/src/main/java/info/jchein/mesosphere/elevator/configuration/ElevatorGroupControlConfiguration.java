package info.jchein.mesosphere.elevator.configuration;

import java.time.ZoneId;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import com.google.common.eventbus.EventBus;
import rx.Scheduler;
import info.jchein.mesosphere.elevator.runtime.SystemRuntimeProperties;
import info.jchein.mesosphere.elevator.domain.common.ElevatorGroupBootstrap;
import info.jchein.mesosphere.elevator.domain.model.ElevatorCar;
import info.jchein.mesosphere.elevator.domain.model.ElevatorGroupControlCompiler;
import info.jchein.mesosphere.elevator.domain.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.emulator.SimulationScenario;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.IRuntimeService;
import info.jchein.mesosphere.elevator.runtime.RuntimeClock;
import info.jchein.mesosphere.elevator.runtime.RuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.RuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.RuntimeService;

@Configuration
public class ElevatorGroupControlConfiguration {


	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	public SimulationScenario getSimulationScenario(
	   IRuntimeClock clock, IRuntimeEventBus eventBus, IRuntimeScheduler scheduler, ElevatorGroupBootstrap bldgProps )
	{
		return new SimulationScenario(clock, eventBus, scheduler, bldgProps);
	}
}
