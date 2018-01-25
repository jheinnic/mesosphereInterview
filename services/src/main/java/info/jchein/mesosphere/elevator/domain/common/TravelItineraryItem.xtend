package info.jchein.mesosphere.elevator.domain.common

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data

@Data
@Buildable
class TravelItineraryItem {
    val int floorIndex
    val DirectionOfTravel travelDirection
    val boolean forPickup
    val boolean forDropOff
}
