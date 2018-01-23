package info.jchein.mesosphere.elevator.simulator.traveller.events

import org.eclipse.xtend.lib.annotations.Data;
import de.oehme.xtend.contrib.Buildable
import info.jchein.mesosphere.elevator.simulator.traveller.TravellerContext

@Data
@Buildable
class LeftElevator implements TravellerEvent {
    override getEventType() { return TravellerEventType.LEFT_ELEVATOR; }	
    val long timeIndex;
    val int carIndex;
    val int floorIndex;
    val TravellerContext travellerContext;
}