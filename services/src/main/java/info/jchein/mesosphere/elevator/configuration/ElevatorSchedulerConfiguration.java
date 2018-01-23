package info.jchein.mesosphere.elevator.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;
import rx.Scheduler.Worker;

import info.jchein.mesosphere.elevator.scheduler.tracking.HeuristicElevatorSchedulingStrategy;
import info.jchein.mesosphere.elevator.scheduler.tracking.ITrafficPredictor;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorSchedulerPort;
import info.jchein.mesosphere.elevator.physics.BuildingProperties;
import info.jchein.mesosphere.elevator.physics.ElevatorDoorProperties;
import info.jchein.mesosphere.elevator.physics.ElevatorMotorProperties;
import info.jchein.mesosphere.elevator.physics.ElevatorWeightProperties;
import info.jchein.mesosphere.elevator.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.physics.PassengerToleranceProperties;

@Configuration
public class ElevatorSchedulerConfiguration {
	@Bean
	@Autowired
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	HeuristicElevatorSchedulingStrategy getElevatorScheduler(final IElevatorSchedulerPort port, final ITrafficPredictor trafficPredictor,
			final BuildingProperties bldgProps, final ElevatorDoorProperties doorProps,
			final ElevatorMotorProperties motorProps, final ElevatorWeightProperties weightProps,
			final PassengerToleranceProperties toleranceProps, final IElevatorPhysicsService physicsService
	) {
		return new HeuristicElevatorSchedulingStrategy(
			port, trafficPredictor, bldgProps, doorProps, motorProps, weightProps, toleranceProps, physicsService);
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

	// @Bean
	// Object getFoo(TestScheduler scheduler) {
	// scheduler.
	// }
}