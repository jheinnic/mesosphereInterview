package info.jchein.mesosphere.elevator.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.scheduler.tracking.HeuristicElevatorSchedulingStrategy;
import info.jchein.mesosphere.elevator.scheduler.tracking.ElevatorSpeedProperties;
import rx.Scheduler.Worker;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

@Configuration
public class ElevatorSchedulerConfiguration {
	private final ElevatorConfigurationProperties configProps;

	@Autowired
	public ElevatorSchedulerConfiguration(ElevatorConfigurationProperties configProps) {
		this.configProps = configProps;
	}

	@Bean
	ElevatorSpeedProperties getSchedulingParameters() {
		return ElevatorSpeedProperties.build(
			builder -> {
				builder.numFloors(this.configProps.numFloors)
				.numElevators(this.configProps.numElevators)
				.slowSpeed(this.configProps.slowSpeed)
				.fastSpeed(this.configProps.fastSpeed) 
				.maxAcceleration(this.configProps.maxAcceleration)
				.maxHallCallWeightPct(this.configProps.maxHallCallWeightPct)
				.maxWeightAllowance(this.configProps.maxWeightAllowance)
				.avgPassengerWeight(this.configProps.avgPassengerWeight)
				.maxStopsForHallCall(this.configProps.maxStopsForHallCall)
				.minDoorHoldTimeOnOpen(this.configProps.minDoorHoldTimeOnOpen)
				.perAccessDoorHoldTime(this.configProps.perAccessDoorHoldTime)
				.slowBarrierForStop(this.configProps.slowBarrierForStop)
				.terminalFloorTime(this.configProps.terminalFloorTime)
				.middleFloorTime(this.configProps.middleFloorTime)
				.singleFloorTime(this.configProps.singleFloorTime);
			}
		);
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	HeuristicElevatorSchedulingStrategy getElevatorScheduler(ElevatorSpeedProperties parameters) {
		return new HeuristicElevatorSchedulingStrategy(parameters);
	}
	
	@Bean
	TestScheduler getScheduler() {
		return Schedulers.test();
	}
	
	@Bean
	Worker getWorker(TestScheduler scheduler) {
		return scheduler.createWorker();
	}
	
//	@Bean
//	Object getFoo(TestScheduler scheduler) {
//		scheduler.
//	}
}