package info.jchein.mesosphere.elevator.simulator.traveller.events

import info.jchein.mesosphere.domain.event.IEvent
import info.jchein.mesosphere.elevator.simulator.traveller.TravellerContext

interface TravellerEvent extends IEvent<TravellerEventType> {
    def long getTimeIndex()
    def TravellerContext getTravellerContext()
}
