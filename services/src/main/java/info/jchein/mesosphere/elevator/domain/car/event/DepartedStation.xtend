package info.jchein.mesosphere.elevator.domain.car.event

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data;
import info.jchein.mesosphere.elevator.domain.car.event.CarEvent
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel

@Buildable
@Data
class DepartedStation implements CarEvent {
	override getEventType() { return CarEventType.DEPARTED_PREVIOUS_FLOOR; }
	val long timeIndex;
	val int carIndex;
	val int floorIndex;
	val DirectionOfTravel direction;
}
