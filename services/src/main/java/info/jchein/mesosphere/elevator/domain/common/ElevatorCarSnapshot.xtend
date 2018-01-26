package info.jchein.mesosphere.elevator.domain.common

import de.oehme.xtend.contrib.Buildable
import java.util.BitSet
import org.eclipse.xtend.lib.annotations.Data

@Data
@Buildable
class ElevatorCarSnapshot {
	long clockTime
	int carIndex
	val BitSet dropOffRequests
	val BitSet upBoundPickups
	val BitSet downBoundPickups
	val ServiceLifecycleStage serviceStage
	val double weightLoad
	val double floorHeight
}