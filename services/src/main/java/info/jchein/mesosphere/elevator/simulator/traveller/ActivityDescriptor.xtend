package info.jchein.mesosphere.elevator.simulator.traveller

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data
import org.hibernate.validator.constraints.NotBlank
import javax.validation.constraints.NotNull
import info.jchein.mesosphere.validator.annotation.Positive

@Data
@Buildable
class ActivityDescriptor {
	@NotNull
	@NotBlank
	val String name

	@NotBlank
	val String aliases

	@NotNull
	val ActivityLocation location

	@Positive
	val double durationMean

	@Positive
	val double durationStdDev
	
	@ActivityCdfDef
	val String nextActivityCdf;
}
