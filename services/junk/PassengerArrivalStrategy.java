package info.jchein.mesosphere.elevator.simulator.model;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import info.jchein.mesosphere.elevator.emulator.model.IEmulatedLandingControls;
import info.jchein.mesosphere.elevator.simulator.passengers.ITravellerQueueService;

@Component
public class PassengerArrivalStrategy implements ITravellerQueueService {

	private List<SimulatedElevatorCar> elevatorControls;
	private IEmulatedLandingControls scenarioControl;

	@Autowired
	public PassengerArrivalStrategy( IEmulatedLandingControls scenarioControl, List<SimulatedElevatorCar> elevatorControls ) {
		this.scenarioControl = scenarioControl;
		this.elevatorControls = elevatorControls;
	}

	@Override
	public void passengerArrival(long timeIndex, int originFloorIndex, int destinationFloorIndex) {
		// TODO Auto-generated method stub
		
	}

}
