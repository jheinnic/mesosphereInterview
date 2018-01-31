package info.jchein.mesosphere.elevator.scheduler.tracking

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data
import java.util.BitSet

@Data
@Buildable
class RecentDeparture {
	val long timeIndex;
    val int floorIndex;
    val info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel initialDirection;
    val double weightBefore
    val double weightAfter;
    private val BitSet potentialDropFloors
    val int lastFloorIndex
}
