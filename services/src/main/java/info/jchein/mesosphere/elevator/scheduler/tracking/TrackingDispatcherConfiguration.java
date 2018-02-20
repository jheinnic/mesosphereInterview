package info.jchein.mesosphere.elevator.scheduler.tracking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.common.bootstrap.EmulatorProperties;
import info.jchein.mesosphere.elevator.common.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorDispatcherPort;
import info.jchein.mesosphere.elevator.scheduler.tracking.HeuristicElevatorSchedulingStrategy;
import rx.Scheduler.Worker;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

@Configuration
public class TrackingDispatcherConfiguration {
	@Bean
	@Autowired
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	HeuristicElevatorSchedulingStrategy getElevatorScheduler(final IElevatorDispatcherPort port,
		final EmulatorProperties rootProps, final IElevatorPhysicsService physicsService
	) {
		return new HeuristicElevatorSchedulingStrategy(port, rootProps, physicsService);
	}

	
//	@Bean
//	@Scope(BeanDefinition.SCOPE_SINGLETON)
//	List<FloorHallSimulation> getFloorHall(List<IEmulatedLandingButtonPanel> emulatedControl) {
//		return emulatedControl.stream().map( control -> new FloorHallSimulation(control)).collect(Collectors.toList());
//	}
}