package info.jchein.mesosphere.elevator.domain.car.event

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data;
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel

@Buildable
@Data
class DepartedLanding implements ElevatorCarEvent {
	val long clockTime
	val long eventIndex
	val int carIndex
	val int origin
	val int destination
	val DirectionOfTravel direction
}
