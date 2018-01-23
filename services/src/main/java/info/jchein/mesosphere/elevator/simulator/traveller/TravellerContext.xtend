package info.jchein.mesosphere.elevator.simulator.traveller

import de.oehme.xtend.contrib.Buildable
import javax.validation.constraints.NotNull
import org.eclipse.xtend.lib.annotations.Data
import javax.validation.constraints.Min

@Data
@Buildable
class TravellerContext
{
	long currentTime;

	@Min(0)
	int workFloorA;

	@Min(0)
	int workFloorB;

	@Min(0)
	int workFloorC;
	
	@Min(0)
	int currentPhaseIndex;

	@Min(0)
	long nextPhaseStartTime;
	
	@NotNull
	ActivityLocation currentLocation
	
	@NotNull
	ActivityLifecycleStage activityLifecycleStage;

	@NotNull
	val String currentActivity;
	
	@NotNull
	val String nextActivity;

	@Min(0)
	long nextActivityStartTime;
}
