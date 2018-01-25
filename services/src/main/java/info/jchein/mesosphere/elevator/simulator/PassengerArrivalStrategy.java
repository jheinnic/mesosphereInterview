package info.jchein.mesosphere.elevator.simulator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import info.jchein.mesosphere.elevator.emulator.ElevatorCarEmulator;
import info.jchein.mesosphere.elevator.emulator.IEmulatedElevatorCar;
import info.jchein.mesosphere.elevator.emulator.IEmulatedLandingButtonPanel;
import info.jchein.mesosphere.elevator.simulator.passengers.IPassengerArrivalStrategy;

@Component
public class PassengerArrivalStrategy implements IPassengerArrivalStrategy {

	private List<ElevatorCarEmulator> elevatorControls;
	private List<? extends IEmulatedLandingButtonPanel> landingPanels;

	@Autowired
	public PassengerArrivalStrategy( List<? extends IEmulatedLandingButtonPanel> landingPanels, List<ElevatorCarEmulator> elevatorControls ) {
		this.landingPanels = landingPanels;
		this.elevatorControls = elevatorControls;
	}

	@Override
	public void passengerArrival(long timeIndex, int originFloorIndex, int destinationFloorIndex) {
		// TODO Auto-generated method stub
		
	}

}
