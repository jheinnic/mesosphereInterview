package info.jchein.mesosphere.elevator.control;


import java.util.BitSet;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.statefulj.framework.core.annotations.FSM;
import org.statefulj.framework.core.model.StatefulFSM;
import org.statefulj.fsm.TooBusyException;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentProperties;
import info.jchein.mesosphere.elevator.control.ElevatorCar;
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
import info.jchein.mesosphere.elevator.control.sdk.IElevatorLandingsPort;
import info.jchein.mesosphere.elevator.runtime.event.IEventBusAdapter;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeScheduler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;


@Slf4j
@Component
public class ElevatorGroupControl
implements IElevatorGroupControl, IElevatorLandingsPort, IDispatchPort
{
   private final DeploymentProperties deploymentProperties;
   private final IDispatchStrategyLocator dispatchLocator;
   private final IRuntimeClock clock;
   private final IRuntimeScheduler scheduler;
   private final IRuntimeEventBus eventBus;
   private final IElevatorCarScope carScope;

   private ImmutableList<? extends IElevatorCar> carList;

   @NotNull
   private final BitSet ascendingCalls;

   @NotNull
   private final BitSet descendingCalls;

   // FSM reference used exclusively for sending an entity-initiating event, effectively using it as a factory method.
   @FSM(ElevatorCar.BEAN_NAME)
   private StatefulFSM<IElevatorCar> fsm;

   private IDispatchStrategy dispatcher;
   private IEventBusAdapter<ElevatorCarEvent> busAdapter;


   @Autowired
   public ElevatorGroupControl( @NotNull DeploymentProperties deploymentProperties, IElevatorCarScope carScope, 
      @NotNull IDispatchStrategyLocator dispatchLocator, @NotNull IEventBusAdapter<ElevatorCarEvent> busAdapter,
      @NotNull IRuntimeScheduler scheduler, @NotNull IRuntimeClock clock, @NotNull IRuntimeEventBus eventBus
      )
   {
      this.deploymentProperties = deploymentProperties;
      this.carScope = carScope;
      this.dispatchLocator = dispatchLocator;
      this.busAdapter = busAdapter;
      this.scheduler = scheduler;
      this.eventBus = eventBus;
      this.clock = clock;
      this.ascendingCalls = new BitSet();
      this.descendingCalls = new BitSet();
   }


   @PostConstruct
   void init()
   {
      this.dispatcher = this.dispatchLocator.locateStrategy();
      this.eventBus.registerListener(this.dispatcher);
      
      final int numElevators = this.deploymentProperties.getBuilding().getNumElevators();
      final ImmutableList.Builder<IElevatorCar> listBuilder = ImmutableList.builder();

      for (int ii = 0; ii < numElevators; ii++) {
         listBuilder.add(
            this.carScope.<IElevatorCar>evalForCar(ii, () -> { try {
               return (IElevatorCar) this.fsm.onEvent(IElevatorCar.Events.ALLOCATED);
            }
            catch (TooBusyException e) {
               log.error("FSM should not be in a blocked state for initial allocation!", e);
               throw new RuntimeException(e);
            }
         }));
      }
      this.carList = listBuilder.build();
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
      return this.busAdapter.toObservable();
   }


   @Subscribe
   public void updateWeightLoad(final WeightLoadUpdated event)
   {
   }


   @Subscribe
   public void onPickupCallAdded(final PickupCallAdded event)
   {
   }


   @Subscribe
   public void onPickupCallRemoved(final PickupCallRemoved event)
   {
   }


   @Subscribe
   public void onDropOffRequested(final DropOffRequested event)
   {
   }


   @Subscribe
   public void onParkedAtLanding(final ParkedAtLanding event)
   {
   }


   @Subscribe
   public void onDepartedLanding(final DepartedLanding event)
   {
   }


   @Subscribe
   public void onPassengerDoorsOpened(final PassengerDoorsOpened event)
   {
   }


   @Subscribe
   public void onPassengerDoorsClosed(final PassengerDoorsClosed event)
   {
   }


   @Subscribe
   public void onTravelledThroughFloor(final TravelledPastFloor event)
   {
   }
   
   @Subscribe
   public void onFloorSensorTriggered(final FloorSensorTriggered event) 
   {
      this.carList.get(event.getCarIndex())
         .notifyFloorSensorTriggered(
            event.getFloorIndex(),
            event.getDirection());
   }


   @Override
   public void assignPickupCall(int floorIndex, DirectionOfTravel departureDirection, int carIndex)
   {
      final IElevatorCar car = this.carList.get(carIndex);
      car.acceptPickupRequest(floorIndex, departureDirection);
   }


   @Override
   public void removePickupCall(int floorIndex, DirectionOfTravel departureDirection, int carIndex)
   {
      final IElevatorCar car = this.carList.get(carIndex);
      car.removePickupRequest(floorIndex, departureDirection);
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
