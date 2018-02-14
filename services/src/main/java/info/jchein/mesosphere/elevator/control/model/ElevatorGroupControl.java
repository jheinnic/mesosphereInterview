package info.jchein.mesosphere.elevator.control.model;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.statefulj.framework.core.annotations.FSM;
import org.statefulj.framework.core.model.StatefulFSM;
import org.statefulj.fsm.TooBusyException;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;

import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentProperties;
import info.jchein.mesosphere.elevator.control.event.ElevatorCarEvent;
import info.jchein.mesosphere.elevator.control.event.Event;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarDriver;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarDriverFactory;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorDispatchingStrategy;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import lombok.SneakyThrows;
import rx.Emitter.BackpressureMode;
import rx.Observable;

@Component
public class ElevatorGroupControl implements IElevatorGroupControl {
   private final DeploymentProperties deploymentProperties;
   private final IElevatorCarDriverFactoryLocator driverFactoryLocator;
	private final ICallDispatcher dispatcher;

	private final IRuntimeEventBus eventBus;
	private final IRuntimeClock clock;
   private final IRuntimeScheduler scheduler;
   
	private ImmutableList<? extends IElevatorCar> carList;
   private Observable<ElevatorCarEvent> changeStream;

   // FSM reference used exclusively for sending an entity-initiating event, effectively using it as a factory method.
   @FSM(ElevatorCar.BEAN_NAME)
   private StatefulFSM<ElevatorCar> fsm;
   
	
	@Autowired
	public ElevatorGroupControl(
	   @NotNull DeploymentProperties deploymentProperties,
		@NotNull IElevatorCarDriverFactoryLocator driverFactoryLocator,
		@NotNull IElevatorDispatchingStrategy dispatchStrategy,
		@NotNull CallDispatcher dispatcher,
		@NotNull IRuntimeScheduler scheduler,
		@NotNull IRuntimeEventBus eventBus,
		@NotNull IRuntimeClock clock
	) {
		this.deploymentProperties = deploymentProperties;
      this.driverFactoryLocator = driverFactoryLocator;
      this.scheduler = scheduler;
		this.eventBus = eventBus;
		this.clock = clock;

      this.dispatcher = dispatcher;
      dispatcher.attachStrategy(dispatchStrategy);
	}

	@SneakyThrows
	@PostConstruct
	void init() {
	   this.changeStream = this.eventBus.<ElevatorCarEvent>toObservable();
	   
	   final int numElevators = this.deploymentProperties.getBuilding().getNumElevators();
	   final String carDriverKey = "emulator"; // this.deploymentProperties.getCarDriverKey();
	   final IElevatorCarDriverFactory adapterFactory = this.driverFactoryLocator.locateDriverFactory(carDriverKey);
	   final ImmutableList.Builder<ElevatorCar> listBuilder = ImmutableList.builder();
	      
	   for( int ii=0; ii<numElevators; ii++ ) {
	      final ElevatorCar elevatorCar = (ElevatorCar) this.fsm.onEvent(ElevatorCar.ALLOCATED);
	      final IElevatorCarDriver portDriver = adapterFactory.allocateDriver(elevatorCar);
	       elevatorCar.attachDriver(portDriver);
	      listBuilder.add(elevatorCar);
	   }
	   
	   this.carList = listBuilder.build();
	}
	


   @Override
   public int getNumElevators()
   {
      return this.deploymentProperties.getBuilding().getNumElevators();
   }


   @Override
   public int getNumFloors()
   {
      return this.deploymentProperties.getBuilding().getNumFloors();
   }


   @Override
   public Observable<ElevatorCarEvent> getChangeStream()
   {
      return this.changeStream;
   }
}
	