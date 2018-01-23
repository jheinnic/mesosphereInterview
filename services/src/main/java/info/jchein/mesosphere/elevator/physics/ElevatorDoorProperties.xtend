package info.jchein.mesosphere.elevator.physics

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data;
import javax.validation.constraints.Min
import info.jchein.mesosphere.validator.annotation.Positive

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
