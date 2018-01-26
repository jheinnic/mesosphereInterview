package info.jchein.mesosphere.elevator.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.configuration.properties.BuildingProperties;
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorDoorProperties;
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorMotorProperties;
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorWeightProperties;
import info.jchein.mesosphere.elevator.configuration.properties.PassengerToleranceProperties;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorSchedulerPort;
import info.jchein.mesosphere.elevator.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.scheduler.tracking.HeuristicElevatorSchedulingStrategy;
import rx.Scheduler.Worker;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

@Configuration
public class ElevatorSchedulerConfiguration {
	@Bean
	@Autowired
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	HeuristicElevatorSchedulingStrategy getElevatorScheduler(final IElevatorSchedulerPort port,
			// final ITrafficPredictor trafficPredictor,
			final BuildingProperties bldgProps, final ElevatorDoorProperties doorProps,
			final ElevatorMotorProperties motorProps, final ElevatorWeightProperties weightProps,
			final PassengerToleranceProperties toleranceProps, final IElevatorPhysicsService physicsService
	) {
		return new HeuristicElevatorSchedulingStrategy(
			port, null, bldgProps, doorProps, motorProps, weightProps, toleranceProps, physicsService);
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	TestScheduler getScheduler() {
		return Schedulers.test();
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	Worker getWorker(TestScheduler scheduler) {
		return scheduler.createWorker();
	}
	
//	@Bean
//	@Scope(BeanDefinition.SCOPE_SINGLETON)
//	List<FloorHallSimulation> getFloorHall(List<IEmulatedLandingButtonPanel> emulatedControl) {
//		return emulatedControl.stream().map( control -> new FloorHallSimulation(control)).collect(Collectors.toList());
//	}
}