package info.jchein.mesosphere.elevator.simulator.traveller.events

import org.eclipse.xtend.lib.annotations.Data;
import de.oehme.xtend.contrib.Buildable
import info.jchein.mesosphere.elevator.simulator.traveller.TravellerContext

@Data
@Buildable
class BeganNextDayPhase implements TravellerEvent {
    override getEventType() { return TravellerEventType.BEGAN_NEXT_DAY_PHASE; }	
    val long timeIndex;
    val TravellerContext travellerContext;
    val int nextDayPhaseIndex;
}