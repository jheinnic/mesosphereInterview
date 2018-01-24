package info.jchein.mesosphere.elevator.physics

import org.eclipse.xtend.lib.annotations.Data
import com.google.common.base.Preconditions
import java.util.Iterator

@Data
class ConstantVelocityPathLeg extends AbstractPathLeg {
	override PathLegType getLegType() {
		return PathLegType.CONSTANT_ACCELERATION
	}

	val double finalHeight
	val double distance

	new(PathMoment moment, double duration) {
		super(moment, duration)

		Preconditions.checkArgument(moment.acceleration == 0, "Constant velocity requires zero acceleration")
		Preconditions.checkArgument(moment.jerk == 0, "Constant velocity requires zero jerk")

		finalHeight = initialHeight + (initialVelocity * duration)
		distance = finalHeight - initialHeight
	}

	override double getFinalAcceleration() {
		return initialAcceleration
	}

	override double getFinalVelocity() {
		return initialVelocity
	}

	def withNewDuration(double newDuration) {
		new ConstantVelocityPathLeg(this.initialMoment, newDuration)
	}

	private static class ConstantVelocityMomentIterator implements Iterator<PathMoment> {
		val ConstantVelocityPathLeg leg
		val double tickDuration
		val double heightUnit
		var PathMoment nextMoment

		new(ConstantVelocityPathLeg leg, double tickDuration) {
			this.leg = leg
			this.nextMoment = leg.initialMoment
			this.tickDuration = tickDuration
			this.heightUnit = tickDuration * leg.finalVelocity
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
				nextMoment = nextMoment.copy[
					time(nextTime).height(nextMoment.height + this.heightUnit)
				]
			}
			
			return retVal
		}

	}

	override momentIterator(double tickDuration) {
		return new ConstantVelocityMomentIterator(this, tickDuration);
	}

}
