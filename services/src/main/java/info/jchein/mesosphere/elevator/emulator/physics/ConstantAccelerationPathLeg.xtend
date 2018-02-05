package info.jchein.mesosphere.elevator.emulator.physics

import com.google.common.base.Preconditions
import org.eclipse.xtend.lib.annotations.Data

@Data
class ConstantAccelerationPathLeg extends AbstractPathLeg {
	override DisplacementFormulaType getDisplacementFormulaType() {
		return DisplacementFormulaType.CONSTANT_ACCELERATION
	}

	val double finalVelocity
	val double finalHeight
	val double distance

	new(PathMoment moment, double duration) {
		super(moment, duration)

		Preconditions.checkArgument(moment.jerk == 0, "Constant acceleration requires zero jerk")

		val double degreeTwo = duration * duration / 2.0
		val double accelerationTwo = initialAcceleration * degreeTwo

		this.finalVelocity = initialVelocity + (initialAcceleration * duration)
		this.finalHeight = initialHeight + (initialVelocity * duration) + accelerationTwo
		this.distance = finalHeight - initialHeight
	}

	override double getFinalAcceleration() {
		return initialAcceleration
	}

	private static class ConstantAccelerationMomentIterator extends AbstractPathLegMomentIterator
	{
		var double velocityDueToAcceleration
		var double heightDueToAcceleration

		new(PathMoment initialMoment, double firstTickDuration, double tickDuration, double finalTickTime) {
			super(initialMoment, firstTickDuration, tickDuration, finalTickTime)
		}

		override setTickDuration(double tickDuration) {
			this.velocityDueToAcceleration = this.getCurrentAcceleration() * tickDuration
			this.heightDueToAcceleration = this.velocityDueToAcceleration * tickDuration / 2.0
		}
//			this.leg = leg
//			this.tickDuration = tickDuration
//			this.velocityDueToAcceleration = leg.initialMoment.acceleration * tickDuration
//			this.heightDueToAcceleration = this.velocityDueToAcceleration * tickDuration / 2.0
//
//			if (leg.initialMoment.time === firstTick) {
//				this.nextMoment == leg.initialMoment
//			} else if ((leg.initialMoment.time < firstTick) && (leg.finalTime >= firstTick)) {
//				val double initialDelta = firstTick - leg.initialMoment.time
//				val double initialHeight = leg.initialMoment.height
//				val double initialVelocity = leg.initialMoment.velocity
//				val double initialAcceleration = leg.initialMoment.acceleration
//
//				val double initialHeightDueToVelocity = initialVelocity * initialDelta
//				val double initialVelocityDueToAcceleration = initialAcceleration * initialDelta
//				val double initialHeightDueToAcceleration = initialVelocityDueToAcceleration * initialDelta / 2.0
//
//				this.nextMoment = leg.initialMoment.copy [
//					time(firstTick)
//					velocity(initialVelocity + initialVelocityDueToAcceleration)
//					height(initialHeight + initialHeightDueToVelocity + initialHeightDueToAcceleration)
//				]
//			}
//		}
//				this.nextMoment.copy [
//					time(nextTime)
//					velocity(this.nextMoment.velocity + this.velocityDueToAcceleration)
//				]
//			}
//
//			return retVal
		
		override double getNextAcceleration() {
			return this.currentAcceleration
		}
		
		override double getNextVelocity() {
			return this.currentVelocity + this.velocityDueToAcceleration
		}
		
		override double getNextHeight() {
			return this.currentHeight + (this.currentVelocity * this.tickDuration) + this.heightDueToAcceleration
		}
	}

	override ConstantAccelerationMomentIterator getMomentIterator(double firstTickDuration, double tickDuration) {
		return new ConstantAccelerationMomentIterator(this.initialMoment, firstTickDuration, tickDuration, this.finalTime)
	}
}
