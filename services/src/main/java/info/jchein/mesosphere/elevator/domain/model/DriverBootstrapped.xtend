package info.jchein.mesosphere.elevator.domain.model

import de.oehme.xtend.contrib.Buildable
import java.util.BitSet
import org.eclipse.xtend.lib.annotations.Data
import info.jchein.mesosphere.elevator.domain.car.event.ElevatorCarEvent
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel

@Buildable
@Data
class DriverBootstrapped implements ElevatorCarEvent {
	val long clockTime
	val int carIndex
	val int floorIndex
	val double weightLoad
	val BitSet dropRequests
	val DirectionOfTravel initialDirection
	val IElevatorCar dispatchTarget
}
