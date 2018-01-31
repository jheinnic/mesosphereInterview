package info.jchein.mesosphere.elevator.domain.event

import de.oehme.xtend.contrib.Buildable
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel
import java.util.BitSet
import org.eclipse.xtend.lib.annotations.Data

@Buildable
@Data
class DriverBootstrapped implements ElevatorCarEvent {
	val long clockTime
	val long carSequence
	val int carIndex
	val int floorIndex
	val double weightLoad
	val BitSet dropRequests
	val DirectionOfTravel initialDirection
}
