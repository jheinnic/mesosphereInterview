package info.jchein.mesosphere.elevator.domain.common

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data

@Data
@Buildable
class TravelItineraryItem {
    val int originFloorIndexß
    val int landingFloorIndex
    val double expectedTravelTime
}
