package info.jchein.mesosphere.elevator.control.model;


import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.statefulj.framework.core.annotations.FSM;
import org.statefulj.framework.core.model.StatefulFSM;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentProperties;
import info.jchein.mesosphere.elevator.control.event.DepartedLanding;
import info.jchein.mesosphere.elevator.control.event.DropOffRequested;
import info.jchein.mesosphere.elevator.control.event.ElevatorCarEvent;
import info.jchein.mesosphere.elevator.control.event.FloorSensorTriggered;
import info.jchein.mesosphere.elevator.control.event.ParkedAtLanding;
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsClosed;
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsOpened;
import info.jchein.mesosphere.elevator.control.event.PickupCallAdded;
import info.jchein.mesosphere.elevator.control.event.PickupCallRemoved;
import info.jchein.mesosphere.elevator.control.event.TravelledPastFloor;
import info.jchein.mesosphere.elevator.control.event.WeightLoadUpdated;
import info.jchein.mesosphere.elevator.control.sdk.IDispatchPort;
import info.jchein.mesosphere.elevator.control.sdk.IDispatchStrategy;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarDriver;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorLandingsPort;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.event.EventBusAdapter;
import info.jchein.mesosphere.elevator.runtime.event.IEventBusAdapter;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import lombok.SneakyThrows;
import rx.Observable;


@Component
public class ElevatorGroupControl
implements IElevatorGroupControl, IElevatorLandingsPort, IDispatchPort
{
   private final DeploymentProperties deploymentProperties;
   private final IElevatorDriverLocator driverLocator;
   private final IDispatchStrategyLocator dispatchLocator;

   private final IRuntimeEventBus eventBus;
   private final IRuntimeClock clock;
   private final IRuntimeScheduler scheduler;

   private final IElevatorCarScope carScope;

   private ImmutableList<? extends IElevatorCar> carList;
   private Observable<ElevatorCarEvent> changeStream;

   // FSM reference used exclusively for sending an entity-initiating event, effectively using it as a factory method.
   @FSM(ElevatorCar.BEAN_NAME)
   private StatefulFSM<ElevatorCar> fsm;
   private IDispatchStrategy dispatcher;
   private IEventBusAdapter<ElevatorCarEvent> busAdapter;


   @Autowired
   public ElevatorGroupControl( @NotNull DeploymentProperties deploymentProperties, IElevatorCarScope carScope, 
      @NotNull IDispatchStrategyLocator dispatchLocator, @NotNull IElevatorDriverLocator driverLocator,
      @NotNull IRuntimeScheduler scheduler,
      @NotNull IRuntimeClock clock,
      @NotNull IRuntimeEventBus eventBus,
      @NotNull IEventBusAdapter<ElevatorCarEvent> busAdapter
      )
   {
      this.deploymentProperties = deploymentProperties;
      this.carScope = carScope;
      this.dispatchLocator = dispatchLocator;
      this.driverLocator = driverLocator;
      this.scheduler = scheduler;
      this.eventBus = eventBus;
      this.clock = clock;
      this.busAdapter = busAdapter;
   }


   @SneakyThrows
   @PostConstruct
   void init()
   {
      this.changeStream = this.busAdapter.toObservable();

      this.dispatcher = this.dispatchLocator.locateStrategy();

      final int numElevators =
         this.deploymentProperties.getBuilding()
            .getNumElevators();
      final ImmutableList.Builder<ElevatorCar> listBuilder = ImmutableList.builder();

      for (int ii = 0; ii < numElevators; ii++) {
         this.carScope.<IElevatorDriverLocator>evalForCar(ii, this.driverLocator, locator -> {addElevator(this.fsm, listBuilder, locator); });
      }

      this.carList = listBuilder.build();
   }

   
   @SneakyThrows
   static void addElevator(StatefulFSM<ElevatorCar> fsm, ImmutableList.Builder<ElevatorCar> listBuilder, IElevatorDriverLocator locator)
   {
      final ElevatorCar elevatorCar = (ElevatorCar) fsm.onEvent(ElevatorCar.ALLOCATED);
      final IElevatorCarDriver portDriver = locator.locateCarDriver();
      elevatorCar.attachDriver(portDriver);
      listBuilder.add(elevatorCar);
   }

   @Override
   public int getNumElevators()
   {
      return this.deploymentProperties.getBuilding()
         .getNumElevators();
   }


   @Override
   public int getNumFloors()
   {
      return this.deploymentProperties.getBuilding()
         .getNumFloors();
   }


   @Override
   public Observable<ElevatorCarEvent> getChangeStream()
   {
      return this.changeStream;
   }


   @Subscribe
   public Object updateWeightLoad(final WeightLoadUpdated event)
   {
      return null;
   }


   @Subscribe
   public Object onPickupCallAdded(final PickupCallAdded event)
   {
      return null;
   }


   @Subscribe
   public Object onPickupCallRemoved(final PickupCallRemoved event)
   {
      return null;
   }


   @Subscribe
   public Object onDropOffRequested(final DropOffRequested event)
   {
      return null;
   }


   @Subscribe
   public Object onParkedAtLanding(final ParkedAtLanding event)
   {
      return null;
   }


   @Subscribe
   public Object onDepartedLanding(final DepartedLanding event)
   {
      return null;
   }


   @Subscribe
   public Object onPassengerDoorsOpened(final PassengerDoorsOpened event)
   {
      return null;
   }


   @Subscribe
   public Object onPassengerDoorsClosed(final PassengerDoorsClosed event)
   {
      return null;
   }


   @Subscribe
   public Object onTravelledThroughFloor(final TravelledPastFloor event)
   {
      return null;
   }


   @Override
   public void assignPickupCall(int floorIndex, DirectionOfTravel departureDirection, int carIndex)
   {
      // TODO Auto-generated method stub

   }


   @Override
   public void removePickupCall(int floorIndex, DirectionOfTravel departureDirection, int carIndex)
   {
      // TODO Auto-generated method stub

   }


   @Override
   public void callForPickup(int floorIndex, DirectionOfTravel direction)
   {
      this.eventBus.post(PickupCallAdded.build(bldr -> {
         bldr.floorIndex(floorIndex)
            .direction(direction);
      }));
   }


   @Override
   public void triggerFloorSensor(int carIndex, int floorIndex, DirectionOfTravel direction)
   {
      this.eventBus.post(FloorSensorTriggered.build(bldr -> {
         bldr.carIndex(carIndex)
            .floorIndex(floorIndex)
            .direction(direction);
      }));
   }

}
