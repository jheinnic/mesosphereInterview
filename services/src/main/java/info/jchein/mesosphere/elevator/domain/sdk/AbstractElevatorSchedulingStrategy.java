package info.jchein.mesosphere.elevator.domain.sdk;

import com.google.common.eventbus.Subscribe;

import info.jchein.mesosphere.elevator.domain.car.event.DropOffRequested;
import info.jchein.mesosphere.elevator.domain.car.event.LocationUpdated;
import info.jchein.mesosphere.elevator.domain.car.event.ParkedAtLanding;
import info.jchein.mesosphere.elevator.domain.car.event.ReadyForDeparture;
import info.jchein.mesosphere.elevator.domain.car.event.SlowedForArrival;
import info.jchein.mesosphere.elevator.domain.car.event.TravelledThroughFloor;
import info.jchein.mesosphere.elevator.domain.car.event.WeightLoadUpdated;
import info.jchein.mesosphere.elevator.domain.common.ElevatorCarSnapshot;
import info.jchein.mesosphere.elevator.domain.hall.event.PickupCallAdded;
import info.jchein.mesosphere.elevator.domain.hall.event.PickupCallRemoved;

/**
 * Implementations of the Elevator Scheduling Strategy are event driven, so there is no call signature to expose beyond
 * and on/off switch.  Implementation wiring to enable satisfaction of the scheduler's functional contract happens by
 * dependency injection and consists primarily of two components:
 * 1)  The system IClock used to register interrupt handlers and schedule asynchronous events 
 * 2)  The EventBus for emitting notifications and registering for notifications from drivers such as the floor button
 *     and indicator light panels, lift motor controls, door mechanisms, operational sensors, and car button panels.
 *     
 * The scheduling algorithm in use may also maintain a data repository to assist its ability to make predictions, but
 * that is an implementation detail of the specific scheduler, not part of the Elevator's integration SDK.
 *  
 * @author jheinnic
 */
public abstract class AbstractElevatorSchedulingStrategy {
   protected final IElevatorSchedulerPort port;

   protected AbstractElevatorSchedulingStrategy(IElevatorSchedulerPort port) {
      this.port = port;
   }

   @Subscribe
	public void updateLocation(LocationUpdated event) { }
	
   @Subscribe
	public void updateWeightLoad(WeightLoadUpdated event) { }
	
   @Subscribe
	public void assignPickupCall(PickupCallAdded event) { }
	
   @Subscribe
	public void onPickupCallRemoved(PickupCallRemoved event) { }
	
   @Subscribe
	public void onDropOffRequested(DropOffRequested event) { }
	
   @Subscribe
	public void onReadyForDeparture(ReadyForDeparture event) { }
	
   @Subscribe
	public void onParkedForBoarding(ParkedAtLanding event) { }
	
//   @Subscribe
//	public void onSlowedForArrival(SlowedForArrival event) { }
	
//   @Subscribe
//	public void onTravelledThroughFloor(TravelledThroughFloor event) { }
}
