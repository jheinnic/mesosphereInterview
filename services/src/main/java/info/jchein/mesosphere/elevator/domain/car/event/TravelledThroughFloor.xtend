package info.jchein.mesosphere.elevator.domain.car.event

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data;

@Buildable
@Data
class TravelledThroughFloor implements ElevatorCarEvent {
	val long clockTime;
	val int carIndex;
	val int floorIndex;
}
