package info.jchein.mesosphere.elevator.configuration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.elevator.domain.common.ElevatorGroupBootstrap;
import info.jchein.mesosphere.elevator.domain.emulator.SimulatedElevatorCar;
import info.jchein.mesosphere.elevator.domain.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarPort;
import ElevatorPhysicsService;
import info.jchein.mesosphere.elevator.runtime.IRuntimeService;
import info.jchein.mesosphere.elevator.simulator.ElevatorSimulation;
import info.jchein.mesosphere.elevator.simulator.IElevatorSimulation;

@Configuration
public class ElevatorSimulationConfiguration {
	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	public IElevatorSimulation getSimulation(IRuntimeService systemClock, EventBus eventBus) {
		return new ElevatorSimulation(systemClock, eventBus);
	}

//	@Bean
//	@Scope(BeanDefinition.SCOPE_SINGLETON)
//	public IElevatorDriverFactory getSimulationElevatorFactory() {
//		return new IElevatorDriverFactory() {
//			@Override
//			public IHallPanelDriver attachFloorHallDriver(IHallPanelPort port) {
//				return getFloorHallDriver(port);
//			}
//
//			@Override
//			public IElevatorCarDriver attachElevatorCarDriver(IElevatorCarPort port) {
//				return getElevatorCarDriver(port);
//			}
//		};
//	}
	
//	@Bean
//	@Scope(BeanDefinition.SCOPE_SINGLETON)
//	PassengerArrivalStrategy getPassengerArrivalStrategy( List<LandingControlEmulator> landingPanels, List<ElevatorCarEmulator> elevatorControls ) {
//		return new PassengerArrivalStrategy(landingPanels, elevatorControls);
//	}

	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	IElevatorPhysicsService getElevatorPhysicsService(ElevatorGroupBootstrap bootstrapData) {
		return new ElevatorPhysicsService(bootstrapData);
	}

//	@Bean
//	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
//	public LandingControlEmulator getFloorHallDriver(IHallPanelPort port) {
//		return new LandingControlEmulator(port);
//	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public SimulatedElevatorCar getElevatorCarDriver(IElevatorCarPort port, IElevatorPhysicsService physicsService) {
		return new SimulatedElevatorCar(port, physicsService);
	}
}
