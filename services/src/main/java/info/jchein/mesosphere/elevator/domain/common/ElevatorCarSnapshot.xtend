package info.jchein.mesosphere.elevator.domain.common

import de.oehme.xtend.contrib.Buildable
import java.util.BitSet
import org.eclipse.xtend.lib.annotations.Data

@Data
@Buildable
class ElevatorCarSnapshot {
	val BitSet dropOffRequests
	val BitSet upBoundPickups
	val BitSet downBoundPickups
	val DirectionOfTravel direction
	val double weightLoad
	val double acceleration
	val double speed
	val double height
}