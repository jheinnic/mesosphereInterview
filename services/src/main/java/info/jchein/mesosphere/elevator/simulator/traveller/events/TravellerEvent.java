package info.jchein.mesosphere.elevator.simulator.traveller.events;

import info.jchein.mesosphere.elevator.simulator.traveller.TravellerContext;

public interface TravellerEvent {
	TravellerEventType getEventType();
    long getTimeIndex();
    TravellerContext getTravellerContext();
}
