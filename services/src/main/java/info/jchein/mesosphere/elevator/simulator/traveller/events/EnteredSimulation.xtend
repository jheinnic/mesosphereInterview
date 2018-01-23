package info.jchein.mesosphere.elevator.simulator.traveller.events

import org.eclipse.xtend.lib.annotations.Data;
import de.oehme.xtend.contrib.Buildable
import info.jchein.mesosphere.elevator.simulator.traveller.TravellerContext

@Data
@Buildable
class EnteredSimulation implements TravellerEvent {
    override getEventType() { return TravellerEventType.ENTERED_SIMULATION; }	
    val long timeIndex;
    val TravellerContext travellerContext;
}