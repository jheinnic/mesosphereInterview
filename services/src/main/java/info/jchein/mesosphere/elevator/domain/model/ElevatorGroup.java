//package info.jchein.mesosphere.elevator.domain.model;
//
//import javax.annotation.PostConstruct;
//import javax.validation.constraints.NotNull;
//
//import com.google.common.collect.ImmutableList;
//import com.google.common.eventbus.EventBus;
//
//import info.jchein.mesosphere.domain.clock.IClock;
//import info.jchein.mesosphere.domain.clock.IInterruptHandler;
//import info.jchein.mesosphere.domain.clock.EventBusInterrupt.EventBusClockEvent;
//import info.jchein.mesosphere.elevator.domain.common.ElevatorCarSnapshot;
//import info.jchein.mesosphere.elevator.domain.common.PickupCallState;
//import info.jchein.mesosphere.elevator.domain.hall.event.PickupCallAdded;
//import info.jchein.mesosphere.elevator.domain.sdk.IElevatorSchedulerDriver;
//
//public class ElevatorGroup implements IElevatorGroup {
////	private final int floorCount;
////	private final int elevatorCount;
//	private final ImmutableList<? extends ILandingControls> hallList;
//	private final ImmutableList<? extends IElevatorCar> carList;
//	private final IElevatorSchedulerDriver scheduler;
//	private final EventBus eventBus;
//	private final IClock systemClock;
//	
//	private final ElevatorCarSnapshot[] carStateModel;
//	private final ClockInterruptHandler interruptHandler;
//	private final MaintenanceEventListener maintenanceHandler;
//	private ITrafficAnalyer analyzer;
//	
//	public ElevatorGroup(
//		@NotNull ImmutableList<? extends ILandingControls> hallList,
//		@NotNull ImmutableList<? extends IElevatorCar> carList,
//		@NotNull IElevatorSchedulerDriver scheduler,
//		@NotNull ITrafficAnalyer analyzer,
//		@NotNull EventBus eventBus,
//		@NotNull IClock systemClock
//	) {
//		this.scheduler = scheduler;
//		this.carList = carList;
//		this.hallList = hallList;
//		this.analyzer = analyzer;
//		this.eventBus = eventBus;
//		this.systemClock = systemClock;
//		this.carStateModel = new ElevatorCarSnapshot[carList.size()];
//
//		this.interruptHandler = new ClockInterruptHandler();
//		this.maintenanceHandler = new MaintenanceEventListener();
//	}
//
//	@PostConstruct
//	void init() {
//		this.eventBus.register(this.maintenanceHandler);
//		this.eventBus.register(this.analyzer);
//
//		int ii = 0;
//		for (final IElevatorCar nextCar: ElevatorGroup.this.carList) {
//			this.carStateModel[ii++] = nextCar.pollForBootstrap();
//		}
//		
//		this.scheduler.bootstrapModel(this.carStateModel);
//		
//		for (final ILandingControls nextLanding: this.hallList) {
//			nextLanding.pollForBootstrap();
//		}
//
//		for (final IElevatorCar nextCar: ElevatorGroup.this.carList) {
//			nextCar.pollForClock();
//		}
//	}
//	
//	public class MaintenanceEventListener {
//		public void assignPickupCall(PickupCallAdded callAdded) {
//			int carIndex = ElevatorGroup.this.scheduler.assignPickupCall(callAdded);
//			final IElevatorCar assignedCar = ElevatorGroup.this.carList.get(carIndex);
//			assignedCar.enqueuePickupRequest(callAdded.getFloorIndex(), callAdded.getDirection());
//		}
//		
//		public void handleClockEvent(EventBusClockEvent clock) {
//			System.out.println("Handled a clock!");
//		}
//	}
//
//	private class ClockInterruptHandler implements IInterruptHandler {
//
//		@Override
//		public void call(Long arg0) {
//			for (final ILandingControls nextLanding: ElevatorGroup.this.hallList) {
//				nextLanding.pollForClock();
//			}
//			for (final IElevatorCar nextCar: ElevatorGroup.this.carList) {
//				nextCar.pollForClock();
//			}
//			ElevatorGroup.this.scheduler.pollForClock();
////			ElevatorGroup.this.analyzer.pollForClock();
//		}
//	}
//}
//	