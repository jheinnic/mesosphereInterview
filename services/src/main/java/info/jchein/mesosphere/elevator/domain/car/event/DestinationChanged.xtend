package info.jchein.mesosphere.elevator.domain.car.event
import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data;
import info.jchein.mesosphere.elevator.domain.car.event.CarEvent
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel

@Data
@Buildable
class DestinationChanged implements CarEvent {
	override getEventType() { return CarEventType.DESTINATION_CHANGED; }

	val long timeIndex;
	val int carIndex;
	val int floorIndex;
	val DirectionOfTravel direction;
}
