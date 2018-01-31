package info.jchein.mesosphere.elevator.domain.event

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data;
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel

@Buildable
@Data
class TravelledThroughFloor implements ElevatorCarEvent {
	val long clockTime
	val long carSequence
	val int carIndex
	val int floorIndex
	val DirectionOfTravel direction
}
