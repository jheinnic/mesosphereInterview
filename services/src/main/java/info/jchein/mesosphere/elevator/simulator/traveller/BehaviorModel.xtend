package info.jchein.mesosphere.elevator.simulator.traveller

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data
import com.google.common.collect.ImmutableList
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Data
@Buildable
class BehaviorModel {
	@NotNull
	final LocationCdf initialLocation;

	@ActivityCdfDef
	val String initialActivityCdf;
	
	@NotNull
	@Size(min=1)
	val ImmutableList<TimeOfDay> timesOfDay
}
