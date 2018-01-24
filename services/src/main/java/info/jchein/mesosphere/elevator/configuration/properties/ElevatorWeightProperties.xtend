package info.jchein.mesosphere.elevator.configuration.properties

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data;
import info.jchein.mesosphere.validator.annotation.Positive

@Data
@Buildable
class ElevatorWeightProperties {
	@Positive
	val double idealWeightLoad
	
	@Positive
	val double maxWeightLoad

	@Positive
	val double passengerWeight
}
