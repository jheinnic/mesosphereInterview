package info.jchein.mesosphere.elevator.scheduler.tracking

import org.eclipse.xtend.lib.annotations.Data
import de.oehme.xtend.contrib.Buildable
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel

@Buildable
@Data
class FloorStopReport {
	val long arrivalTime
	val long departureTime
	val int floorIndex
	val int carIndex
	val DirectionOfTravel departureDirection
	// val int estimatedArrivals
	// val int estimatedDepartures
	// val int estimatedPassengerCount
	// val double weightDifference
	val double weightOnArrival
	val double weightOnDeparture
	val boolean answeredPickupCall
	val boolean answeredDropOffCall
}
