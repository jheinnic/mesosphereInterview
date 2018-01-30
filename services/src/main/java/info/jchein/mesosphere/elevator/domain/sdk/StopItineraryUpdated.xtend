package info.jchein.mesosphere.elevator.domain.sdk

import org.eclipse.xtend.lib.annotations.Data
import de.oehme.xtend.contrib.Buildable
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel
import java.util.BitSet

@Data
@Buildable
class StopItineraryUpdated {
	val long clockTime
	val int carIndex
	val DirectionOfTravel initialDirection
	val int reverseAfter
	val BitSet upwardStops
	val BitSet downwardStops
}
