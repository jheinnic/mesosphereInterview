package info.jchein.mesosphere.elevator.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.domain.clock.IClock;
import info.jchein.mesosphere.elevator.configuration.properties.BuildingProperties;
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorDoorProperties;
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorMotorProperties;
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorWeightProperties;
import info.jchein.mesosphere.elevator.configuration.properties.PassengerToleranceProperties;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarDriver;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarPort;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorDriverFactory;
import info.jchein.mesosphere.elevator.domain.sdk.IHallPanelPort;
import info.jchein.mesosphere.elevator.emulator.ElevatorCarEmulator;
import info.jchein.mesosphere.elevator.emulator.IEmulatedElevatorCar;
import info.jchein.mesosphere.elevator.emulator.IEmulatedLandingButtonPanel;
import info.jchein.mesosphere.elevator.emulator.LandingControlEmulator;
import info.jchein.mesosphere.elevator.physics.ElevatorPhysicsService;
import info.jchein.mesosphere.elevator.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.domain.sdk.IHallPanelDriver;
import info.jchein.mesosphere.elevator.simulator.ElevatorSimulation;
import info.jchein.mesosphere.elevator.simulator.IElevatorSimulation;
import info.jchein.mesosphere.elevator.simulator.PassengerArrivalStrategy;
import info.jchein.mesosphere.elevator.simulator.FloorHallSimulation;
import rx.Scheduler.Worker;

@Configuration
public class ElevatorSimulationConfiguration {
	private final ElevatorMotorProperties motorProps;

	@Autowired
	public ElevatorSimulationConfiguration(ElevatorMotorProperties _motorProps) {
		this.motorProps = _motorProps;
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	public IElevatorSimulation getSimulation(IClock systemClock, EventBus eventBus,
			List<LandingControlEmulator> landingList, List<ElevatorCarEmulator> carList) {
		return new ElevatorSimulation(systemClock, eventBus, landingList, carList);
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	public IElevatorDriverFactory getSimulationElevatorFactory() {
		return new IElevatorDriverFactory() {
			@Override
			public IHallPanelDriver attachFloorHallDriver(IHallPanelPort port) {
				return getFloorHallDriver(port);
			}

			@Override
			public IElevatorCarDriver attachElevatorCarDriver(IElevatorCarPort port) {
				return getElevatorCarDriver(port);
			}
		};
	}
	
	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	PassengerArrivalStrategy getPassengerArrivalStrategy( List<LandingControlEmulator> landingPanels, List<ElevatorCarEmulator> elevatorControls ) {
		return new PassengerArrivalStrategy(landingPanels, elevatorControls);
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	IElevatorPhysicsService getElevatorPhysicsService(BuildingProperties bldgProperties, ElevatorDoorProperties doorProps,
		ElevatorMotorProperties motorProps, ElevatorWeightProperties weightProps, PassengerToleranceProperties toleranceProps) {
		return new ElevatorPhysicsService(bldgProperties, doorProps, motorProps, weightProps, toleranceProps);
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public LandingControlEmulator getFloorHallDriver(IHallPanelPort port) {
		return new LandingControlEmulator(port);
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public ElevatorCarEmulator getElevatorCarDriver(IElevatorCarPort port) {
		return new ElevatorCarEmulator(port, null, this.motorProps);
	}
}
