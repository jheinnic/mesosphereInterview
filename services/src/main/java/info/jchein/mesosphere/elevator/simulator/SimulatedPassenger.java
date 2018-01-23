package info.jchein.mesosphere.elevator.simulator;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

import org.hibernate.validator.constraints.ScriptAssert;

import info.jchein.mesosphere.validator.annotation.UUIDString;

@ScriptAssert.List({
	@ScriptAssert(lang="javascript", script="_this.ingressFloorIndex < _this.egressFloorIndex")
})
public class SimulatedPassenger {
	@NotNull
	@UUIDString
	private final String uuid;
	
	@Min(0)
	private final int ingressFloorIndex;
	
	@Min(0)
	private final int egressFloorIndex;
	
	@Past
	private long pickupCallTime;

	SimulatedPassenger(String uuid, int ingressFloorIndex, int egressFloorIndex, long pickupCallTime ) {
		this.uuid = uuid;
		this.ingressFloorIndex = ingressFloorIndex;
		this.egressFloorIndex = egressFloorIndex;
		this.pickupCallTime = pickupCallTime;
	}
}
