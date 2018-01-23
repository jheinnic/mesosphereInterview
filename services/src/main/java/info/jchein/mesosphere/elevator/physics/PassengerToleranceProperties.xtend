package info.jchein.mesosphere.elevator.physics

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data;
import info.jchein.mesosphere.validator.annotation.Positive
import javax.validation.constraints.NotNull
import javax.validation.constraints.Min

@Data
@Buildable
class PassengerToleranceProperties {
	@Positive
	val double refusePickupAfterWeightPct

	@Min(0)
	val int passengerStopTolerance
	
	@Positive
	val double passengerTravelTimeTolerance
	
	@Positive
	val double passengerPickupTimeTolerance
	
	@NotNull
	val ServiceLevelGoal slaPriorityOne 

	@NotNull
	val ServiceLevelGoal slaPriorityTwo 

	@NotNull
	val ServiceLevelGoal slaPriorityThree 
}
