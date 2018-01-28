package info.jchein.mesosphere.elevator.configuration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.domain.clock.IClock;
import info.jchein.mesosphere.domain.clock.SystemClock;
import info.jchein.mesosphere.elevator.configuration.properties.BuildingProperties;
import info.jchein.mesosphere.elevator.configuration.properties.SystemRuntimeProperties;
import info.jchein.mesosphere.elevator.domain.model.ElevatorCar;
import info.jchein.mesosphere.elevator.domain.model.ElevatorCarService;
import info.jchein.mesosphere.elevator.emulator.SimulationScenario;
import info.jchein.mesosphere.elevator.physics.IElevatorPhysicsService;
import rx.Scheduler;

@Configuration
public class ElevatorGroupControlConfiguration {
//	@Bean
//	@Autowired
//	@Scope(BeanDefinition.SCOPE_SINGLETON)
//	public ElevatorGroup getElevatorModelRoot(
//		AbstractElevatorSchedulingStrategy scheduler, IClock systemClock, EventBus eventBus,
//		BuildingProperties bldgProps)
//	{
//		final int floorCount = bldgProps.getNumFloors();
//		final int elevatorCount = bldgProps.getNumElevators();
//
//		ImmutableList.Builder<LandingControls> hallListBuilder = ImmutableList.<LandingControls>builder();
//		ImmutableList.Builder<ElevatorCar> carListBuilder = ImmutableList.<ElevatorCar>builder();
//
//		for (int ii = 0; ii < floorCount; ii++ ) {
//			final LandingControls port = getLandingPort(systemClock, eventBus, bldgProps);
//			port.attachDriver(
//				driverFactory.attachFloorHallDriver(port));
//			hallListBuilder.add(port);
//		}
//
//		for (int ii = 0; ii < elevatorCount; ii++) {
//			final ElevatorCar port = getElevatorCarPort(systemClock, eventBus);
//			port.attachDriver(
//				driverFactory.attachElevatorCarDriver(port));
//			carListBuilder.add(port);
//		}
//		
//		return new ElevatorGroup(
//			hallListBuilder.build(), carListBuilder.build(), scheduler, null, eventBus, systemClock);
//	}
	
	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	public EventBus getEventBus() {
		return new EventBus();
	}
	
	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	public SystemClock getSystemClock(Scheduler scheduler, SystemRuntimeProperties runtimeProperties) {
		return new SystemClock(runtimeProperties, scheduler);
	}
	
	// TODO: This declaration should be replaced by use of the IElevatorCarService to specify bootstrap car count as
	//       driven by configuration.
	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public ElevatorCar getElevatorCarPort(IClock systemClock, EventBus eventBus, IElevatorPhysicsService physicsService) {
		return new ElevatorCar(systemClock, eventBus, physicsService);
	}
	
	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	public ElevatorCarService getElevatorCarService(IClock systemClock, EventBus eventBus, Scheduler systemScheduler) {
	   return new ElevatorCarService(eventBus, systemClock, systemScheduler);
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	public SimulationScenario getSimulationScenario(IClock systemClock, EventBus eventBus, BuildingProperties bldgProperties) {
		return new SimulationScenario(systemClock, eventBus, bldgProperties);
	}
}
