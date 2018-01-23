package info.jchein.mesosphere.elevator.domain.hall.event

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data;
import info.jchein.mesosphere.elevator.domain.hall.event.HallEvent
import info.jchein.mesosphere.elevator.domain.hall.event.HallEventType
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel

@Buildable
@Data
class AvailableCarSignalOff implements HallEvent {
	override getEventType() { return HallEventType.AVAILABLE_CAR_SIGNAL_OFF; }
	val long timeIndex;
	val int floorIndex;
	val DirectionOfTravel direction;
}
