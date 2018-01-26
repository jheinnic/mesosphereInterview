package info.jchein.mesosphere.elevator.simulator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import info.jchein.mesosphere.elevator.emulator.ElevatorCarSimulator;
import info.jchein.mesosphere.elevator.emulator.ISimulationScenario;
import info.jchein.mesosphere.elevator.simulator.passengers.IPassengerArrivalStrategy;

@Component
public class PassengerArrivalStrategy implements IPassengerArrivalStrategy {

	private List<ElevatorCarSimulator> elevatorControls;
	private ISimulationScenario scenarioControl;

	@Autowired
	public PassengerArrivalStrategy( ISimulationScenario scenarioControl, List<ElevatorCarSimulator> elevatorControls ) {
		this.scenarioControl = scenarioControl;
		this.elevatorControls = elevatorControls;
	}

	@Override
	public void passengerArrival(long timeIndex, int originFloorIndex, int destinationFloorIndex) {
		// TODO Auto-generated method stub
		
	}

}
