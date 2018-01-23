package info.jchein.mesosphere.elevator.domain.hall.event

import de.oehme.xtend.contrib.Buildable
import info.jchein.mesosphere.elevator.domain.hall.event.HallEvent
import info.jchein.mesosphere.elevator.domain.hall.event.HallEventType
import org.eclipse.xtend.lib.annotations.Data
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel

@Buildable
@Data
class FloorSensorTriggered implements HallEvent {
	override getEventType() { return HallEventType.FLOOR_SENSOR_TRIGGERED; }
	
	val long timeIndex;
	val int floorIndex;
	val int carIndex;
	val DirectionOfTravel direction;
}
