package info.jchein.mesosphere.elevator.control.event

import de.oehme.xtend.contrib.Buildable
import info.jchein.mesosphere.elevator.common.DirectionOfTravel
import org.eclipse.xtend.lib.annotations.Data

@Buildable
@Data
class AvailableCarSignalOn implements LandingEvent {
	val long clockTime
	val long floorSequence
	val int floorIndex
	val DirectionOfTravel direction
}
