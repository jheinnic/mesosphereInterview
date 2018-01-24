package info.jchein.mesosphere.elevator.configuration.properties

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
	val info.jchein.mesosphere.elevator.physics.ServiceLevelGoal slaPriorityOne 

	@NotNull
	val info.jchein.mesosphere.elevator.physics.ServiceLevelGoal slaPriorityTwo 

	@NotNull
	val info.jchein.mesosphere.elevator.physics.ServiceLevelGoal slaPriorityThree 
}
