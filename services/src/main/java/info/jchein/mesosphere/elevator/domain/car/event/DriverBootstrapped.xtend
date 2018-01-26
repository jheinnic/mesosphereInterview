package info.jchein.mesosphere.elevator.domain.car.event

import de.oehme.xtend.contrib.Buildable
import java.util.BitSet
import org.eclipse.xtend.lib.annotations.Data

@Buildable
@Data
class DriverBootstrapped implements ElevatorCarEvent {
	val long clockTime;
	val int carIndex;
}
