package info.jchein.mesosphere.elevator.domain.car.event

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data;
import info.jchein.mesosphere.elevator.domain.car.event.CarEvent
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel
import info.jchein.mesosphere.elevator.domain.common.SpeedOfTravel

@Data
@Buildable
class DestinationRetained implements CarEvent {
	override CarEventType getEventType() { return CarEventType.DESTINATION_RETAINED; }

	val long timeIndex;
	val int carIndex;
	val int rejectedDestination;
	val int retainedDestination;
	val DirectionOfTravel currentDirection;
	val SpeedOfTravel currentSpeed;
	val String reason;
}
