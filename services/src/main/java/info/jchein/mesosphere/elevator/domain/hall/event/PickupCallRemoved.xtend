package info.jchein.mesosphere.elevator.domain.hall.event

import de.oehme.xtend.contrib.Buildable
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel
import org.eclipse.xtend.lib.annotations.Data

@Data
@Buildable
class PickupCallRemoved implements HallEvent {
	val long clockTime;
	val int floorIndex;
	val DirectionOfTravel direction;
}
