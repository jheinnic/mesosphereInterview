package info.jchein.mesosphere.elevator.simulator.model;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.control.IElevatorGroupControl;
import info.jchein.mesosphere.elevator.control.event.DepartedLanding;
import info.jchein.mesosphere.elevator.control.event.ParkedAtLanding;
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsClosed;
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsOpened;
import info.jchein.mesosphere.elevator.control.event.TravelledPastFloor;
import info.jchein.mesosphere.elevator.emulator.model.IEmulatorControl;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.simulator.event.PickupRequested;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ElevatorSimulation
{
   private final IEmulatorControl emulatedControl;
   private final ImmutableList<Queue<ISimulatedTraveller>> upwardBoundPickups;
   private final ImmutableList<Queue<ISimulatedTraveller>> downwardBoundPickups;
   private final ImmutableList<ImmutableList<Queue<ISimulatedTraveller>>> passengerDropOffs;
   private final int floorCount;
   private final int carCount;


   @Autowired
   public ElevatorSimulation( IEmulatorControl emulatedControl, IElevatorGroupControl groupControl,
      IRuntimeEventBus eventBus )
   {
      this.emulatedControl = emulatedControl;
      this.floorCount = groupControl.getNumFloors();
      this.carCount = groupControl.getNumElevators();

      final ImmutableList.Builder<Queue<ISimulatedTraveller>> upBuilder =
         ImmutableList.<Queue<ISimulatedTraveller>> builder();
      final ImmutableList.Builder<Queue<ISimulatedTraveller>> downBuilder =
         ImmutableList.<Queue<ISimulatedTraveller>> builder();
      final ImmutableList.Builder<ImmutableList<Queue<ISimulatedTraveller>>> passengerBuilder =
         ImmutableList.<ImmutableList<Queue<ISimulatedTraveller>>> builder();

      for (int ii = 1; ii < floorCount; ii++) {
         upBuilder.add(new LinkedList<ISimulatedTraveller>());
         downBuilder.add(new LinkedList<ISimulatedTraveller>());
      }
      this.upwardBoundPickups = upBuilder.build();
      this.downwardBoundPickups = downBuilder.build();

      for (int ii = 0; ii < this.carCount; ii++) {
         final ImmutableList.Builder<Queue<ISimulatedTraveller>> floorBuilder =
            ImmutableList.<Queue<ISimulatedTraveller>> builder();
         for (int jj = 0; jj < this.floorCount; jj++) {
            floorBuilder.add(new LinkedList<ISimulatedTraveller>());
         }
         passengerBuilder.add(floorBuilder.build());
      }
      this.passengerDropOffs = passengerBuilder.build();

      // There has to be a simpler way to do this and there has to be a way to be able to defer receiving
      // events until this next clock pulse! TODO!
      eventBus.registerListener(this);
   }


   @Subscribe
   public void onPickupRequested(final PickupRequested event)
   {
      final int pickupFloor = event.getOriginIndex();
      final int dropOffFloor = event.getDestinationIndex();
      final DirectionOfTravel direction = event.getDirection();
      
      log.info("Called onPickupRequested: {}", event);

      Preconditions
         .checkArgument(pickupFloor != dropOffFloor, "Origin and destination must be different");
      Preconditions.checkArgument(
         pickupFloor >= 0 && pickupFloor < this.floorCount,
         "Origin floor must be non-negative and within building");
      Preconditions.checkArgument(
         dropOffFloor >= 0 && dropOffFloor < this.floorCount,
         "Destination floor must be non-negative and within building");
      Preconditions.checkArgument(
         ((direction == DirectionOfTravel.GOING_UP) && (pickupFloor < dropOffFloor)) ||
            ((direction == DirectionOfTravel.GOING_DOWN) && (dropOffFloor < pickupFloor)),
         "Direction and floor indices must be consistent");

      final Queue<ISimulatedTraveller> pickupQueue;
      if (direction == DirectionOfTravel.GOING_UP) {
         pickupQueue = this.upwardBoundPickups.get(pickupFloor);
      } else {
         pickupQueue = this.downwardBoundPickups.get(pickupFloor - 1);
      }

      final ISimulatedTraveller traveller = event.getTraveller();
      pickupQueue.offer(traveller);
      this.emulatedControl.callForPickup(pickupFloor, direction);
      traveller.queueForPickup();
   }


   @Subscribe
   public void onParkedAtLanding(final ParkedAtLanding event)
   {}


   @Subscribe
   public void onDepartedLanding(final DepartedLanding event)
   {}


   @Subscribe
   public void onPassengerDoorsOpened(final PassengerDoorsOpened event)
   {
      final Queue<ISimulatedTraveller> pickupQueue;
      final Queue<ISimulatedTraveller> dropOffQueue;
      final DirectionOfTravel direction;

      if (event.getDirection() == DirectionOfTravel.GOING_UP) {
         pickupQueue = ElevatorSimulation.this.upwardBoundPickups.get(event.getFloorIndex());
         direction = DirectionOfTravel.GOING_UP;
      } else {
         pickupQueue = ElevatorSimulation.this.downwardBoundPickups.get(event.getFloorIndex() - 1);
         direction = DirectionOfTravel.GOING_DOWN;
      }
      dropOffQueue =
         ElevatorSimulation.this.passengerDropOffs.get(event.getCarIndex())
            .get(event.getFloorIndex());

      ElevatorSimulation.this.emulatedControl
         .updateManifest(event.getCarIndex(), event.getFloorIndex(), direction, mbldr -> {
            Iterator<ISimulatedTraveller> nextIter = dropOffQueue.iterator();
            while (nextIter.hasNext()) {
               final ISimulatedTraveller nextTraveller = nextIter.next();
               mbldr.disembark(nextTraveller.getWeight());
               nextTraveller.disembarkElevator();
               nextIter.remove();
            }

            nextIter = pickupQueue.iterator();
            while (nextIter.hasNext()) {
               final ISimulatedTraveller nextTraveller = nextIter.next();
               if (mbldr.board(nextTraveller.getWeight())) {
                  mbldr.requestDropOff(nextTraveller.getDestinationFloor());
                  nextIter.remove();
                  nextTraveller.boardElevator(event.getCarIndex());
               } else {
                  log.warn("Over capacity while loading elevator car");
                  break;
               }
            }

            // TODO: Verify that this will allow the full car to leave without preventing a second car for answering the call.
            if (pickupQueue.isEmpty() == false) {
               ElevatorSimulation.this.emulatedControl.callForPickup(event.getFloorIndex(), direction);
            }
         });
   }


   @Subscribe
   public void onPassengerDoorsClosed(final PassengerDoorsClosed event) { }


   @Subscribe
   public void onTravelledThroughFloor(final TravelledPastFloor event) { }
}
