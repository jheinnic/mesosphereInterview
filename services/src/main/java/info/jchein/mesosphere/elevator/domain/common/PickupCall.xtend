package info.jchein.mesosphere.elevator.domain.common

import org.eclipse.xtend.lib.annotations.Data;
import de.oehme.xtend.contrib.Buildable
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel

@Data
@Buildable
class PickupCall {
    val long timeOfRequest;
    val int floorIndex;
    val DirectionOfTravel direction;
    val ElevatorCarSnapshot assignedCar;
}
