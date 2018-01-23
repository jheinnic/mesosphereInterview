package info.jchein.mesosphere.elevator.configuration.workloads;

import java.util.List;

import org.javasim.streams.UniformStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.domain.clock.IClock;
import info.jchein.mesosphere.elevator.emulator.LandingControlEmulator;
import info.jchein.mesosphere.elevator.simulator.passengers.IPassengerArrivalStrategy;
import info.jchein.mesosphere.elevator.simulator.passengers.IPathSelector;
import info.jchein.mesosphere.elevator.simulator.passengers.RandomPathSelector;
import info.jchein.mesosphere.elevator.simulator.passengers.SimulatedSimplePassengerSource;
import info.jchein.mesosphere.elevator.simulator.passengers.SimulatedTravellingPassengerSource;
import rx.Scheduler.Worker;

@Profile({ "workload.morning" })
@Configuration
public class MorningTrafficPatternConfiguration {
	private IClock systemClock;
	private LandingControlEmulator groundFloor;
	private IPassengerArrivalStrategy arrivalStrategy;

	@Autowired
	public MorningTrafficPatternConfiguration(
		List<LandingControlEmulator> floors, IClock systemClock, IPassengerArrivalStrategy arrivalStrategy)
	{
		this.systemClock = systemClock;
		this.arrivalStrategy = arrivalStrategy;
		for (final LandingControlEmulator nextFloor : floors) {
			if (nextFloor.getFloorIndex() == 0) {
				this.groundFloor = nextFloor;
				break;
			}
		}
	}
	
	@Bean
	@Scope(BeanDefinition.SCOPE_SINGLETON)
	public IPathSelector getPathSelector() {
		UniformStream uniformStream = new UniformStream(0, 1);
		RandomPathSelector retVal = new RandomPathSelector(32, uniformStream);
		retVal.generatePaths(250, 5, true, true, 0, 32);
		return retVal;
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public SimulatedSimplePassengerSource getPassengerSource(
		Worker schedulingWorker, int destinationFloor, double interarrivalTime)
	{
		return new SimulatedSimplePassengerSource(
			this.systemClock, interarrivalTime, this.groundFloor.getFloorIndex(), destinationFloor, this.arrivalStrategy);
	}
	
	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public SimulatedTravellingPassengerSource getTravellingPassengerSource() {
		return new SimulatedTravellingPassengerSource(
			this.systemClock, null, 50, 9000, 60000, 1000, getPathSelector(), this.arrivalStrategy);
	}

}
