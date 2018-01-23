package info.jchein.mesosphere.elevator.domain.model;

import javax.validation.constraints.NotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

import info.jchein.mesosphere.elevator.domain.sdk.IElevatorSchedulerDriver;

public class ElevatorGroup implements IElevatorGroup {
//	private final int floorCount;
//	private final int elevatorCount;
	private final ImmutableList<LandingControls> hallList;
	private final ImmutableList<ElevatorCar> carList;
	private final IElevatorSchedulerDriver scheduler;
	private EventBus eventBus;
	
	
	public ElevatorGroup(
		@NotNull ImmutableList<LandingControls> hallList,
		@NotNull ImmutableList<ElevatorCar> carList,
		@NotNull IElevatorSchedulerDriver scheduler,
		@NotNull EventBus eventBus
	) {
		this.scheduler = scheduler;
		this.carList = carList;
		this.hallList = hallList;
		this.eventBus = eventBus;
//		this.elevatorCount = carList.size();
//		this.floorCount = hallList.size();
	}
	
	/*@PostConstruct
	public void initScheduler() {
		this.assignCarListener(
			this.scheduler.getCarEventListener());
		this.assignHallListener(
			this.scheduler.getHallEventListener());
	}*/

	/*
	@Override
	public void assignCarListener(IEventListener<CarEvent> listener) {
		this.carEventBus.subscribe(listener);
	}

	@Override
	public void removeCarListener(IEventListener<CarEvent> listener) {
		this.carEventBus.unsubscribe(listener);
	}

	@Override
	public void assignHallListener(IEventListener<HallEvent> listener) {
		this.hallEventBus.subscribe(listener);
	}

	@Override
	public void removeHallListener(IEventListener<HallEvent> listener) {
		this.hallEventBus.unsubscribe(listener);
	}
	*/
}

/*
	@Override
	public Map<FloorCall, Integer> getHallCallMatching() {
		return null;
	}

	@Override
	public void handleFloorProximityTrigger(int carIndex, int floorIndex, DirectionOfTravel direction) {
	}

	@Override
	public void handleHallCallTrigger(int floorIndex, DirectionOfTravel direction) {
	}

	@Override
	public void handleCarCallTrigger(int carIndex, int floorIndex) {
	}
}
*/