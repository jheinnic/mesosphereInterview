package info.jchein.mesosphere.elevator.domain.car.event

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data;
import info.jchein.mesosphere.elevator.domain.car.event.CarEvent

@Buildable
@Data
class ReadyForDeparture implements CarEvent {
	override getEventType() { return CarEventType.CLEARED_FOR_DEPARTURE; }
	val long timeIndex;
	val int carIndex;
	val int floorIndex;
	val double weightOnDeparture;
}
