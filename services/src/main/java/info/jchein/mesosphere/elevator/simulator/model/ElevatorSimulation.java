package info.jchein.mesosphere.elevator.simulator.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.control.event.DepartedLanding;
import info.jchein.mesosphere.elevator.control.event.DropOffRequested;
import info.jchein.mesosphere.elevator.control.event.ElevatorCarEvent;
import info.jchein.mesosphere.elevator.control.event.ParkedAtLanding;
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsClosed;
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsOpened;
import info.jchein.mesosphere.elevator.control.event.PickupCallAdded;
import info.jchein.mesosphere.elevator.control.event.PickupCallRemoved;
import info.jchein.mesosphere.elevator.control.event.TravelledThroughFloor;
import info.jchein.mesosphere.elevator.control.event.WeightLoadUpdated;
import info.jchein.mesosphere.elevator.control.model.IElevatorGroupControl;
import info.jchein.mesosphere.elevator.emulator.model.IEmulatorControl;
import rx.Observable;

@Component
@Scope(scopeName="prototype")
public class ElevatorSimulation
implements ITravellerQueueService
{
   private static final Logger log = LoggerFactory.getLogger(ElevatorSimulation.class);

	private final IEmulatorControl emulatedControl;
	private final ImmutableList<Queue<ISimulatedTraveller>> upwardBoundPickups;
	private final ImmutableList<Queue<ISimulatedTraveller>> downwardBoundPickups;
	private final ImmutableList<ImmutableList<Queue<ISimulatedTraveller>>> passengerDropOffs;
   private final int floorCount;
   private final int carCount;

   private ElevatorChangeHandler changeHandler;

   private EventBus localDispatch;

   private Observable<ElevatorCarEvent> changeStream;

	@Autowired
	public ElevatorSimulation(IEmulatorControl emulatedControl, IElevatorGroupControl groupControl) {
		this.emulatedControl = emulatedControl;
		this.floorCount = emulatedControl.getNumFloors();
		this.carCount = emulatedControl.getNumElevators();

		final ImmutableList.Builder<Queue<ISimulatedTraveller>> upBuilder =
		   ImmutableList.<Queue<ISimulatedTraveller>>builder();
		final ImmutableList.Builder<Queue<ISimulatedTraveller>> downBuilder =
		   ImmutableList.<Queue<ISimulatedTraveller>>builder();
		final ImmutableList.Builder<ImmutableList<Queue<ISimulatedTraveller>>> passengerBuilder =
		   ImmutableList.<ImmutableList<Queue<ISimulatedTraveller>>>builder();

		for (int ii=1; ii<floorCount; ii++) {
		   upBuilder.add(new LinkedList<ISimulatedTraveller>());
		   downBuilder.add(new LinkedList<ISimulatedTraveller>());
		}
		this.upwardBoundPickups = upBuilder.build();
		this.downwardBoundPickups = downBuilder.build();
		
		for (int ii=0; ii<this.carCount; ii++) {
		   final ImmutableList.Builder<Queue<ISimulatedTraveller>> floorBuilder =
		      ImmutableList.<Queue<ISimulatedTraveller>>builder();
		   for (int jj=0; jj<this.floorCount; jj++) {
		      floorBuilder.add(new LinkedList<ISimulatedTraveller>());
		   }
		   passengerBuilder.add(floorBuilder.build());
		}
		this.passengerDropOffs = passengerBuilder.build();
		
		this.changeHandler = new ElevatorChangeHandler();
		this.localDispatch = new EventBus();
		this.localDispatch.register(this.changeHandler);
		
		this.changeStream = groupControl.getChangeStream();
		this.changeStream.subscribe(
		   next -> { this.localDispatch.post(next); });
	}
	
   @Override
   public void queueForPickup(ISimulatedTraveller pickupRequest) 
   {
      final int pickupFloor = pickupRequest.getCurrentFloor();
      final int dropOffFloor = pickupRequest.getDestinationFloor();
      
		Preconditions.checkArgument(pickupFloor != dropOffFloor, "Origin and destination must be different");
		Preconditions.checkArgument(pickupFloor >= 0 && pickupFloor < this.floorCount, "Origin floor must be non-negative and within building");
		Preconditions.checkArgument(dropOffFloor >= 0 && dropOffFloor < this.floorCount, "Destination floor must be non-negative and within building");

		final Queue<ISimulatedTraveller> pickupQueue;
		final DirectionOfTravel direction;
		if (pickupFloor < dropOffFloor) {
		   pickupQueue = this.upwardBoundPickups.get(pickupFloor);
		   direction = DirectionOfTravel.GOING_UP;
		} else {
		   pickupQueue = this.downwardBoundPickups.get(pickupFloor - 1);
		   direction = DirectionOfTravel.GOING_DOWN;
		}
		
		pickupQueue.offer(pickupRequest);
		this.emulatedControl.callForPickup(pickupFloor, direction);
		pickupRequest.onQueuedForPickup();
	}
   
   private class ElevatorChangeHandler {
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
      public void onPassengerDoorsOpened(final PassengerDoorsOpened event)
      {
			final Queue<ISimulatedTraveller> pickupQueue;
			final Queue<ISimulatedTraveller> dropOffQueue;
			final DirectionOfTravel direction;

			if (event.getDirection() == DirectionOfTravel.GOING_UP) {
			   pickupQueue = ElevatorSimulation.this.upwardBoundPickups.get(
			      event.getFloorIndex());
			   direction = DirectionOfTravel.GOING_UP;
			} else {
			   pickupQueue = ElevatorSimulation.this.downwardBoundPickups.get(
			      event.getFloorIndex() - 1);
			   direction = DirectionOfTravel.GOING_DOWN;
			}
			dropOffQueue = ElevatorSimulation.this.passengerDropOffs.get(
			   event.getCarIndex()
			).get(
			   event.getFloorIndex()
			);
			
			ElevatorSimulation.this.emulatedControl.updateManifest(event.getCarIndex(), event.getFloorIndex(), direction, mbldr -> {
			   Iterator<ISimulatedTraveller> nextIter = dropOffQueue.iterator();
			   while (nextIter.hasNext()) {
			      ISimulatedTraveller nextTraveller = nextIter.next();
			      if (mbldr.disembark(nextTraveller.getWeight())) {
			         nextIter.remove();
			         nextTraveller.onSuccessfulDropOff();
			      } else {
			         log.error("Could not remove passengers??");
			         break;
			      }
			   }
			   
			   nextIter = pickupQueue.iterator();
			   while (nextIter.hasNext()) {
			      ISimulatedTraveller nextTraveller = nextIter.next();
			      if (mbldr.board(nextTraveller.getWeight())) {
			         mbldr.requestStop(nextTraveller.getDestinationFloor());
			         nextIter.remove();
			         nextTraveller.onSuccessfulPickup(
			            event.getCarIndex()
			         );
			      } else {
			         log.warn("Over capacity while loading elevator car");
			         break;
			      }
			   }
			   
			   if (pickupQueue.isEmpty() == false) {
			      ElevatorSimulation.this.emulatedControl.callForPickup(event.getFloorIndex(), direction);
			   }
			});
      }


      @Subscribe
      public Object onPassengerDoorsClosed(final PassengerDoorsClosed event)
      {
         return null;
      }


      @Subscribe
      public Object onTravelledThroughFloor(final TravelledThroughFloor event)
      {
         return null;
      }
   }
}
