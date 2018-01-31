package info.jchein.mesosphere.elevator.domain.physics

import java.util.Iterator
import org.eclipse.xtend.lib.annotations.Data

@Data
class ConstantJerkPathLeg extends AbstractPathLeg {
	override PathLegType getLegType() {
		return PathLegType.CONSTANT_JERK
	}

	val double finalAcceleration
	val double finalVelocity
	val double finalHeight

	val double distance

	new(PathMoment moment, double duration) {
		super(moment, duration)

		val double degreeTwo = duration * duration / 2.0
		val double jerkTwo = jerk * degreeTwo
		val double accelerationTwo = initialAcceleration * degreeTwo
		val double jerkThree = jerkTwo * duration / 3.0

		finalAcceleration = initialAcceleration + (jerk * duration)
		finalVelocity = initialVelocity + (initialAcceleration * duration) + jerkTwo
		finalHeight = initialHeight + (initialVelocity * duration) + accelerationTwo + jerkThree
		distance = finalHeight - initialHeight
	}

	private static class ConstantJerkMomentIterator implements Iterator<PathMoment> {
		val ConstantJerkPathLeg leg
		val double tickDuration
		val double tickDegreeTwo  // For use when calculating next position
		val double accelerationUnit
		val double velocityPartial
		val double heightPartial

		var PathMoment nextMoment

		new(ConstantJerkPathLeg leg, double tickDuration) {
			this.leg = leg
			this.nextMoment = leg.initialMoment
			this.tickDuration = tickDuration
			this.tickDegreeTwo = tickDuration * tickDuration / 2.0
			this.accelerationUnit = tickDuration * leg.jerk
			this.velocityPartial = this.accelerationUnit * tickDuration / 2.0
			this.heightPartial = this.velocityPartial * tickDuration / 3.0
		}
		
		new(ConstantJerkPathLeg leg, double tickDuration, double firstTick) {
			this.leg = leg
			this.tickDuration = tickDuration
			this.tickDegreeTwo = tickDuration * tickDuration / 2.0
			this.accelerationUnit = tickDuration * leg.jerk
			this.velocityPartial = this.accelerationUnit * tickDuration / 2.0
			this.heightPartial = this.velocityPartial * tickDuration / 3.0

			val double tickDelta = firstTick - leg.initialMoment.time
			this.nextMoment = PathMoment.build[
				time(firstTick)
					.acceleration(nextMoment.acceleration + (tickDelta * leg.jerk))
					.velocity(
						nextMoment.velocity + (nextMoment.acceleration * tickDelta) + this.velocityPartial)
					.height(
						nextMoment.height + (nextMoment.velocity * tickDelta) +
							(nextMoment.acceleration * tickDelta * tickDelta / 2.0) + 
							(nextMoment.jerk * tickDelta * tickDelta * tickDelta / 6.0)
						)
					.jerk(nextMoment.jerk)
				]
		}

		override hasNext() {
			nextMoment !== null
		}

		override next() {
			val retVal = nextMoment
			val nextTime = nextMoment.time + tickDuration
			if (nextTime >= leg.finalTime) {
				nextMoment = null
			} else {
				nextMoment = PathMoment.build[
					time(nextTime)
					.acceleration(nextMoment.acceleration + this.accelerationUnit)
					.velocity(
						nextMoment.velocity + (nextMoment.acceleration * tickDuration) + this.velocityPartial)
					.height(
						nextMoment.height + (nextMoment.velocity * tickDuration) +
							(nextMoment.acceleration * tickDegreeTwo) + this.heightPartial)
					.jerk(nextMoment.jerk)
				]
			}

			return retVal
		}

	}

	override momentIterator(double tickDuration) {
		return new ConstantJerkMomentIterator(this, tickDuration);
	}

	override momentIterator(double tickDuration, double firstTick) {
		return new ConstantJerkMomentIterator(this, tickDuration, firstTick);
	}
}
