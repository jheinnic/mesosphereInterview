package info.jchein.mesosphere.elevator.physics

import java.util.Iterator
import java.util.NoSuchElementException

class JourneyPathLegIterator implements Iterator<PathLeg>, Iterable<PathLeg> {
	val JourneyArc source
	var JourneyPathLegRole nextRole
	
	new(JourneyArc source) {
		this.source = source
		this.nextRole = JourneyPathLegRole.FORWARD_JERK_ONE;
	}
	
	override hasNext() {
		this.nextRole !== null
	}
	
	override next() {
		val retVal = switch (this.nextRole) {
			case JourneyPathLegRole.FORWARD_JERK_ONE: {
				this.source.forwardJerkOne
			}
			case JourneyPathLegRole.FORWARD_ACCELERATION: {
				this.source.forwardAcceleration
			}
			case JourneyPathLegRole.REVERSE_JERK_ONE: {
				this.source.reverseJerkOne
			}
			case JourneyPathLegRole.CONSTANT_VELOCITY: {
				this.source.constantVelocity
			}
			case JourneyPathLegRole.REVERSE_JERK_TWO: {
				this.source.reverseJerkTwo
			}
			case JourneyPathLegRole.REVERSE_ACCELERATION: {
				this.source.reverseAcceleration
			}
			case JourneyPathLegRole.FORWARD_JERK_TWO: {
				this.source.forwardJerkTwo
			}
			case JourneyPathLegRole.TERMINAL_SEGMENT: {
				this.source.terminalSegment
			}
			default: {
				throw new NoSuchElementException();
			}
		}

		this.nextRole = this.nextRole.nextLeg;
		return retVal;
	}
	
	override iterator() {
		return this
	}
	
}