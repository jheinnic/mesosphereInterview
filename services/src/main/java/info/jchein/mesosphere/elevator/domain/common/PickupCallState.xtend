package info.jchein.mesosphere.elevator.domain.common

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data

@Data
@Buildable
class PickupCallState {
    val int floorIndex
    val boolean goingUp
    val boolean goingDown
}
