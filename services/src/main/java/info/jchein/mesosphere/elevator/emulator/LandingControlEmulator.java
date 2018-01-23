package info.jchein.mesosphere.elevator.emulator;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.domain.sdk.IHallPanelPort;
import info.jchein.mesosphere.elevator.simulator.SimulatedPassenger;
import info.jchein.mesosphere.elevator.domain.sdk.IHallPanelDriver;

@Component
@Scope(scopeName="prototype")
public class LandingControlEmulator
implements IHallPanelDriver, IEmulatedLandingButtonPanel
{
	private enum FloorIndicatorType {
		OFF,
		CALL,
		SIGNAL,
		BOTH
	}

	// In lieu of physical buttons and lights, these fields act as a proxy for the state of emulated indicator lights.
	private FloorIndicatorType up = FloorIndicatorType.OFF;
	private FloorIndicatorType down = FloorIndicatorType.OFF;

	private final IHallPanelPort port;
	private final int floorIndex;

	@Autowired
	public LandingControlEmulator(IHallPanelPort port) {
		this.port = port;
		this.floorIndex = port.getFloorIndex();
	}
	
	public int getFloorIndex() {
		return this.floorIndex;
	}
	
	public void pressCallButton(DirectionOfTravel direction) {
		this.port.requestPickup(direction);
	}

	@Override
	public void parkArrivingCar(DirectionOfTravel direction, int carIndex) {
		this.port.lightParkedSignal(carIndex, direction);
		this.port.turnOffHallCall(direction);
	}

//	@Override
//	public void cancelHallCall(DirectionOfTravel direction) {
//		this.port.turnOffHallCall(direction);
//	}

	@Override
	public void launchDepartingCar(DirectionOfTravel direction, int carIndex) {
		this.port.turnOffHallCall(direction);
		this.port.dimParkedSignal(carIndex, direction);
	}
}
