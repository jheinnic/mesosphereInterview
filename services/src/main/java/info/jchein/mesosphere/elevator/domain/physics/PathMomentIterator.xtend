package info.jchein.mesosphere.elevator.domain.physics

import java.util.Iterator
import java.util.NoSuchElementException

class PathMomentIterator implements Iterator<PathMoment>, Iterable<PathMoment> {
	val Iterator<PathLeg> legSource
	var PathLeg currentLeg
	var Iterator<PathMoment> currentMomentSource
	var PathMoment lastMoment

	double tickDuration

	new(Iterator<PathLeg> legSource, double tickDuration) {
		this.legSource = legSource
		this.currentLeg = legSource.next()
		this.tickDuration = tickDuration
		this.currentMomentSource = this.currentLeg.momentIterator(this.tickDuration)
	}

	override hasNext() {
		this.currentMomentSource.hasNext() || this.legSource.hasNext()
	}

	override next() {
		if (! this.currentMomentSource.hasNext()) {
			if (this.legSource.hasNext()) {
				if (this.lastMoment === null) {
					this.currentMomentSource = this.legSource.next().momentIterator(this.tickDuration)
				} else {
					this.currentMomentSource = this.legSource.next()
						.momentIterator(
							this.tickDuration, this.lastMoment.time + this.tickDuration
						)
				}
			} else {
				throw new NoSuchElementException()
			}
		}
		return this.currentMomentSource.next()
	}
	
	override iterator() {
		return this
	}
}

/*
 * private static class AbstractMomentIterator implements Iterator<PathMoment> {
 * 	val PathLeg leg
 * 	val double heightUnit
 * 	val double tickDuration
 * 	var PathMoment nextMoment

 * 	new(ConstantVelocityPathLeg leg, double tickDuration) {
 * 		this.leg = leg
 * 		this.nextMoment = leg.initialMoment
 * 		this.tickDuration = tickDuration
 * 		this.heightUnit = tickDuration * leg.finalVelocity
 * 	}

 * 	override hasNext() {
 * 		nextMoment !== null
 * 	}

 * 	override next() {
 * 		val retVal = nextMoment
 * 		val nextTime = nextMoment.time + tickDuration
 * 		if (nextTime >= leg.finalTime) {
 * 			nextMoment = null
 * 		} else {
 * 			nextMoment = nextMoment.copy [
 * 				time(nextTime).height(nextMoment.height + this.heightUnit)
 * 			]
 * 		}

 * 		return retVal
 * 	}
 * }
 */
