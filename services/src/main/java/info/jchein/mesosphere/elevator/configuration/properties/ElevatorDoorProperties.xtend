package info.jchein.mesosphere.elevator.configuration.properties

import de.oehme.xtend.contrib.Buildable
import info.jchein.mesosphere.validator.annotation.Positive
import org.eclipse.xtend.lib.annotations.Data

@Data
@Buildable
class ElevatorDoorProperties {
	@Positive
	val double minDoorHoldTimePerOpen

	@Positive
	val double doorHoldTimePerPerson

	@Positive
	val double doorOpenCloseSlideTime
}
