package info.jchein.mesosphere.elevator.domain.common

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data

@Data
@Buildable
class DropOffRequest {
    val long timeOfRequest;
    val int floorIndex;
}
