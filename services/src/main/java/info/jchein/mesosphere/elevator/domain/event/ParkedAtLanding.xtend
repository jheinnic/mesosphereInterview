package info.jchein.mesosphere.elevator.domain.event

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data

@Buildable
@Data
class ParkedAtLanding implements ElevatorCarEvent {
	val long clockTime;
    val long carSequence;
	val int carIndex;
	val int floorIndex;
}
