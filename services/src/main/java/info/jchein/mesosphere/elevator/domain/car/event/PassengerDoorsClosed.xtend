package info.jchein.mesosphere.elevator.domain.car.event

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data;

@Buildable
@Data
class PassengerDoorsClosed implements ElevatorCarEvent {
	val long clockTime;
	val int carIndex;
}
