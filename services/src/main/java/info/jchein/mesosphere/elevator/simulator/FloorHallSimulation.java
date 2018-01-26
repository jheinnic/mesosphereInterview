package info.jchein.mesosphere.elevator.simulator;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.emulator.ISimulationScenario;
import info.jchein.mesosphere.elevator.simulator.passengers.IPassengerArrivalStrategy;

@Component
@Scope(scopeName="prototype")
public class FloorHallSimulation
implements IPassengerArrivalStrategy
{
	private final ISimulationScenario emulatedControl;
	private final ArrayList<SimulatedPassenger> upwardBoundPickups =
		new ArrayList<SimulatedPassenger>(10);
	private final ArrayList<SimulatedPassenger> downwardBoundPickups =
		new ArrayList<SimulatedPassenger>(10);

	@Autowired
	public FloorHallSimulation(ISimulationScenario emulatedControl) {
		this.emulatedControl = emulatedControl;
	}
	
	@Override
	public void passengerArrival(long timeIndex, int originFloorIndex, int destinationFloorIndex) {
		Preconditions.checkArgument(originFloorIndex != destinationFloorIndex, "Origin and destination must be different");
		Preconditions.checkArgument(originFloorIndex >= 0, "Origin floor must be non-negative and within building");
		Preconditions.checkArgument(destinationFloorIndex >= 0, "Destination floor must be non-negative and within building");

		final DirectionOfTravel direction;
		if (originFloorIndex < destinationFloorIndex) {
			direction = DirectionOfTravel.GOING_UP;
			final UUID uuid = UUID.randomUUID();
			final SimulatedPassenger p =
				new SimulatedPassenger(uuid.toString(), destinationFloorIndex, destinationFloorIndex, timeIndex);
			this.upwardBoundPickups.add(p);
		} else {
			direction = DirectionOfTravel.GOING_DOWN;
			final UUID uuid = UUID.randomUUID();
			final SimulatedPassenger p =
				new SimulatedPassenger(uuid.toString(), destinationFloorIndex, destinationFloorIndex, timeIndex);
			this.downwardBoundPickups.add(p);
		}
	}
}
