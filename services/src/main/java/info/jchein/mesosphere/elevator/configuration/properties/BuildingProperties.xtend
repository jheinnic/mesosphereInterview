package info.jchein.mesosphere.elevator.configuration.properties

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data;
import javax.validation.constraints.Min
import info.jchein.mesosphere.validator.annotation.Positive

@Data
@Buildable
class BuildingProperties {
	@Min(3)
	val int numFloors

	@Min(1)
	val int numElevators

	@Positive
	val double metersPerFloor
}
