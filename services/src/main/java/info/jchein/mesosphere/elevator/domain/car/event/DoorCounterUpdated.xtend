package info.jchein.mesosphere.elevator.domain.car.event

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data;
import info.jchein.mesosphere.elevator.domain.car.event.CarEvent

@Buildable
@Data
class DoorCounterUpdated implements CarEvent {
	override getEventType() { return CarEventType.DOOR_COUNTER_UPDATED; }
	val long timeIndex;
	val int carIndex;
	val int lastValue;
	val int currentValue;
	val int totalChange;
}
