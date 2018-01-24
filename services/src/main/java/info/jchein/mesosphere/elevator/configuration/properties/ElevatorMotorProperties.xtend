package info.jchein.mesosphere.elevator.configuration.properties

import de.oehme.xtend.contrib.Buildable

import org.eclipse.xtend.lib.annotations.Data;
import info.jchein.mesosphere.validator.annotation.Positive

@Data
@Buildable
class ElevatorMotorProperties {
	@Positive
	val double brakingSpeed
	
	@Positive
	val double brakingDistance

	@Positive
	val double maxRiseSpeed

	@Positive
	val double maxDescentSpeed

	@Positive
	val double maxAcceleration
	
	@Positive
	val double maxJerk
}
