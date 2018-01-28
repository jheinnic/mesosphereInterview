package info.jchein.mesosphere.elevator.domain.model;

import info.jchein.mesosphere.elevator.domain.car.event.DropOffRequested;
import info.jchein.mesosphere.elevator.domain.car.event.ParkedAtLanding;
import info.jchein.mesosphere.elevator.domain.hall.event.PickupCallAdded;

public interface ITrafficAnalyer {
	public void onPickupRequested(PickupCallAdded event);
	
	public void onDropOffRequested(DropOffRequested event);
	
	public void onParkedForBoarding(ParkedAtLanding event);
}
