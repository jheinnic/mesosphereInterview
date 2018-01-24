package info.jchein.mesosphere.elevator.physics

import org.eclipse.xtend.lib.annotations.Data
import com.google.common.base.Preconditions
import java.util.Iterator

@Data
class ConstantAccelerationPathLeg extends AbstractPathLeg {
    override PathLegType getLegType() {
    		return PathLegType.CONSTANT_ACCELERATION
    }	
    
	val double finalVelocity
	val double finalHeight
	val double distance

	new(PathMoment moment, double duration) {
		super(moment, duration)

		Preconditions.checkArgument(moment.jerk == 0, "Constant acceleration requires zero jerk")

		val double degreeTwo = duration * duration / 2.0
		val double accelerationTwo = initialAcceleration  * degreeTwo

		finalVelocity = initialVelocity + (initialAcceleration * duration)
		finalHeight = initialHeight + (initialVelocity * duration) + accelerationTwo
		distance = finalHeight - initialHeight
	}
	
	override double getFinalAcceleration() { 
		return initialAcceleration
	}
	
	
	private static class ConstantAccelerationMomentIterator implements Iterator<PathMoment> {
		val ConstantAccelerationPathLeg leg
		val double tickDuration
		val double velocityUnit
		val double heightPartial

		var PathMoment nextMoment

		new(ConstantAccelerationPathLeg leg, double tickDuration) {
			this.leg = leg
			this.nextMoment = leg.initialMoment
			this.tickDuration = tickDuration
			this.velocityUnit = leg.finalAcceleration * tickDuration
			this.heightPartial = this.velocityUnit * tickDuration / 2.0
		}

		override hasNext() {
			nextMoment !== null
		}

		override next() {
			val retVal = this.nextMoment
			val nextTime = this.nextMoment.time + this.tickDuration
			if (nextTime >= leg.finalTime) {
				this.nextMoment = null
			} else {
				this.nextMoment = this.nextMoment.copy[
					time(nextTime)
					.velocity(this.nextMoment.velocity + this.velocityUnit)
					.height(
						nextMoment.height + (nextMoment.velocity * tickDuration) + this.heightPartial)
				]
			}

			return retVal
		}

	}

	override momentIterator(double tickDuration) {
		return new ConstantAccelerationMomentIterator(this, tickDuration);
	}
	
}