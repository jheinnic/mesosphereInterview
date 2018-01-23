package info.jchein.mesosphere.elevator.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.domain.clock.IClock;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarDriver;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorCarPort;
import info.jchein.mesosphere.elevator.domain.sdk.IElevatorDriverFactory;
import info.jchein.mesosphere.elevator.domain.sdk.IHallPanelPort;
import info.jchein.mesosphere.elevator.emulator.LandingControlEmulator;
import info.jchein.mesosphere.elevator.domain.sdk.IHallPanelDriver;
import info.jchein.mesosphere.elevator.simulator.ElevatorSimulation;
import info.jchein.mesosphere.elevator.simulator.IElevatorSimulation;
import info.jchein.mesosphere.elevator.simulator.FloorHallSimulation;
import info.jchein.mesosphere.elevator.simulator.ElevatorCarEmulator;
import rx.Scheduler.Worker;

@Configuration
public class ElevatorSimulationConfiguration
{
	private ElevatorConfigurationProperties configProps;

	@Autowired
	public ElevatorSimulationConfiguration( ElevatorConfigurationProperties _configProps )
	{
		this.configProps = _configProps;
	}
	
	@Bean
	public IElevatorSimulation getSimulation(
		IClock systemClock, EventBus eventBus, List<LandingControlEmulator> landingList, List<ElevatorCarEmulator> carList)
	{
		return new ElevatorSimulation(systemClock, eventBus, landingList, carList);
	}

	@Bean
	public IElevatorDriverFactory getSimulationElevatorFactory()
	{
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
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public IHallPanelDriver getFloorHallDriver(IHallPanelPort port)
	{
		return new FloorHallEmulator(port);
	}
	
	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public IElevatorCarDriver getElevatorCarDriver(IElevatorCarPort port)
	{
		return new ElevatorCarEmulator(port);
	}
}
