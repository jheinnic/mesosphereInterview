package info.jchein.mesosphere.elevator.scheduler.tracking

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data

@Data
@Buildable
class CallPrediction {
	long callTime
	boolean genuinePickup
	int originFloorIndex
	int destinationFloorIndex
}
