package info.jchein.mesosphere.elevator.domain.sdk;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.domain.common.ElevatorCarSnapshot;
import info.jchein.mesosphere.elevator.domain.common.TravelItineraryItem;
import info.jchein.mesosphere.elevator.domain.hall.event.FloorSensorTriggered;

/**
 * An interface of entry points from hardware interrupt handlers 
 * @author jheinnic
 *
 */
public interface IElevatorCarDriver {
	ElevatorCarSnapshot pollForBootstrap();
	ElevatorCarSnapshot pollForService(TravelItineraryItem[] destinationQueue);
}
