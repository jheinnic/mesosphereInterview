package info.jchein.mesosphere.elevator.domain.hall.event

import de.oehme.xtend.contrib.Buildable
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel
import org.eclipse.xtend.lib.annotations.Data

@Data
@Buildable
class PickupCallAdded implements HallEvent {
	override getEventType() { return HallEventType.PICKUP_CALL_ADDED; }
	
	val long timeIndex;
	val int floorIndex;
	val DirectionOfTravel direction;
}
