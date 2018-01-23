package info.jchein.mesosphere.elevator.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.domain.clock.IClock;
import info.jchein.mesosphere.elevator.domain.model.ElevatorCar;
import info.jchein.mesosphere.elevator.domain.model.ElevatorGroup;
import info.jchein.mesosphere.elevator.domain.model.LandingControls;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorDriverFactory;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorSchedulerDriver;
import info.jchein.mesosphere.elevator.physics.BuildingProperties;

@Configuration
public class ElevatorGroupControlConfiguration {
	@Bean
	@Autowired
	public ElevatorGroup getElevatorModelRoot(
		IElevatorSchedulerDriver scheduler, IElevatorDriverFactory driverFactory, IClock systemClock, EventBus eventBus,
		BuildingProperties bldgProps)
	{
		final int floorCount = bldgProps.getNumFloors();
		final int elevatorCount = bldgProps.getNumElevators();

		ImmutableList.Builder<LandingControls> hallListBuilder = ImmutableList.<LandingControls>builder();
		ImmutableList.Builder<ElevatorCar> carListBuilder = ImmutableList.<ElevatorCar>builder();

		for (int ii = 0; ii < floorCount; ii++ ) {
			final LandingControls port = getLandingPort(ii, systemClock, eventBus, elevatorCount);
			port.attachDriver(
				driverFactory.attachFloorHallDriver(port));
			hallListBuilder.add(port);
		}

		for (int ii = 0; ii < elevatorCount; ii++) {
			final ElevatorCar port = getElevatorCarPort(ii, systemClock, eventBus);
			port.attachDriver(
				driverFactory.attachElevatorCarDriver(port));
			carListBuilder.add(port);
		}
		
		return new ElevatorGroup(
			hallListBuilder.build(), carListBuilder.build(), scheduler, eventBus);
	}
	
	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	public EventBus getEventBus() {
		return new EventBus();
	}
	
	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public ElevatorCar getElevatorCarPort(int carIndex, IClock systemClock, EventBus eventBus) {
		return new ElevatorCar(carIndex, systemClock, eventBus);
	}
	
	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public LandingControls getLandingPort(int floorIndex, IClock systemClock, EventBus eventBus, int elevatorCount) {
		return new LandingControls(floorIndex, systemClock, eventBus, elevatorCount);
	}
}
