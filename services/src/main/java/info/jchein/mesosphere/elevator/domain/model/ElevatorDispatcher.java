package info.jchein.mesosphere.elevator.domain.model;

import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.domain.clock.EventBusInterrupt.EventBusClockEvent;
import info.jchein.mesosphere.elevator.configuration.properties.BuildingProperties;
import info.jchein.mesosphere.elevator.domain.hall.event.PickupCallAdded;

public class ElevatorDispatcher {
	private final EventBus eventBus;

	public ElevatorDispatcher(EventBus eventBus, BuildingProperties bldgProps) {
		this.eventBus = eventBus;
	}
	public void assignPickupCall(PickupCallAdded callAdded) {
		int carIndex = ElevatorGroup.this.scheduler.assignPickupCall(callAdded);
		final IElevatorCar assignedCar = ElevatorGroup.this.carList.get(carIndex);
		assignedCar.enqueuePickupRequest(callAdded.getFloorIndex(), callAdded.getDirection());
	}
	
	public void handleClockEvent(EventBusClockEvent clock) {
		System.out.println("Handled a clock!");
	}
}
