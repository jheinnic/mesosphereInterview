package info.jchein.mesosphere.elevator.control.sdk;

import com.google.common.eventbus.Subscribe;

import info.jchein.mesosphere.elevator.control.event.DepartedLanding;
import info.jchein.mesosphere.elevator.control.event.DropOffRequested;
import info.jchein.mesosphere.elevator.control.event.ParkedAtLanding;
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsClosed;
import info.jchein.mesosphere.elevator.control.event.PassengerDoorsOpened;
import info.jchein.mesosphere.elevator.control.event.TravelledPastFloor;
import info.jchein.mesosphere.elevator.control.event.WeightLoadUpdated;
import info.jchein.mesosphere.elevator.control.event.PickupCallAdded;
import info.jchein.mesosphere.elevator.control.event.PickupCallRemoved;

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
   protected final IElevatorDispatcherPort port;

   protected AbstractElevatorSchedulingStrategy(IElevatorDispatcherPort port) {
      this.port = port;
   }

   @Subscribe
	public void updateWeightLoad(WeightLoadUpdated event) { }
	
   @Subscribe
	public void onPickupCallAdded(PickupCallAdded event) { }
	
   @Subscribe
	public void onPickupCallRemoved(PickupCallRemoved event) { }
	
   @Subscribe
	public void onDropOffRequested(DropOffRequested event) { }
	
   @Subscribe
	public void onParkedAtLanding(ParkedAtLanding event) { }
	
   @Subscribe
	public void onDepartedLanding(DepartedLanding event) { }
	
   @Subscribe
	public void onPassengerDoorsOpened(PassengerDoorsOpened event) { }
	
   @Subscribe
	public void onPassengerDoorsClosed(PassengerDoorsClosed event) { }
	
//   @Subscribe
//	public void onSlowedForArrival(SlowedForArrival event) { }
	
   @Subscribe
	public void onTravelledThroughFloor(TravelledPastFloor event) { }
}
