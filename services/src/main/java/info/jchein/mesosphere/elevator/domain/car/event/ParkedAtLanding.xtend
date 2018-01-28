package info.jchein.mesosphere.elevator.domain.car.event

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data;
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel

@Buildable
@Data
class ParkedAtLanding implements ElevatorCarEvent {
	val long clockTime;
	val int carIndex;
	val int floorIndex;
	val DirectionOfTravel direction
}
