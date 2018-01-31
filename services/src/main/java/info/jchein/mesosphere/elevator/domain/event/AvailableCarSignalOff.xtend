package info.jchein.mesosphere.elevator.domain.event

import de.oehme.xtend.contrib.Buildable
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel
import org.eclipse.xtend.lib.annotations.Data

@Buildable
@Data
class AvailableCarSignalOff implements LandingEvent {
	val long clockTime;
	val long floorSequence;
	val int floorIndex;
	val DirectionOfTravel direction;
}
