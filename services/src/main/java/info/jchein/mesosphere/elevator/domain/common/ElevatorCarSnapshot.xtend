package info.jchein.mesosphere.elevator.domain.common

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data
import com.google.common.collect.ImmutableList
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel
import info.jchein.mesosphere.elevator.domain.common.SpeedOfTravel
import info.jchein.mesosphere.elevator.domain.common.DoorState

@Data
@Buildable
class ElevatorCarSnapshot {
	val ImmutableList<DropOffRequest> dropOffRequests
	val ImmutableList<TravelItineraryItem> currentItinerary
	val int currentDestination
	val double currentFloorHeight
	val double currentWeightLoad
	val DirectionOfTravel direction
	val SpeedOfTravel speed
	val DoorState doorState
}