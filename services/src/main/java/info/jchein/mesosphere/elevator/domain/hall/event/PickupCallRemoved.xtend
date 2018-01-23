package info.jchein.mesosphere.elevator.domain.hall.event

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data;
import info.jchein.mesosphere.elevator.domain.hall.event.HallEvent
import info.jchein.mesosphere.elevator.domain.hall.event.HallEventType
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel

@Buildable
@Data
class PickupCallRemoved implements HallEvent {
	override getEventType() { return HallEventType.PICKUP_CALL_REMOVED; }
	
	val long timeIndex;
	val int floorIndex;
	val DirectionOfTravel direction;
}
