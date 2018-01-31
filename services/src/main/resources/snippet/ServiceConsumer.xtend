package info.jchein.mesosphere.elevator.scheduler.tracking

import org.eclipse.xtend.lib.annotations.Data
import org.jgrapht.util.FibonacciHeap

@Data
class ServiceConsumer {
	val int arrivalFloor
	val int destinationFloor
	val int carBoarded
	val int passedStopCount
	val double sojournTime
	val double boardingWaitTime
	val double weight
}