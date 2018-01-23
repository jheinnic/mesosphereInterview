package info.jchein.mesosphere.elevator.simulator.traveller.events

import org.eclipse.xtend.lib.annotations.Data;
import de.oehme.xtend.contrib.Buildable
import info.jchein.mesosphere.elevator.simulator.traveller.TravellerContext

@Data
@Buildable
class CalledForElevator implements TravellerEvent {
    override getEventType() { return TravellerEventType.CALLED_FOR_ELEVATOR; }	
    val long timeIndex;
    val int floorIndex
    val TravellerContext travellerContext;
    
}