package info.jchein.mesosphere.elevator.common.physics

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

	private static class ConstantAccelerationMomentIterator extends AbstractPathLegMomentIterator {
		var double velocityDueToAcceleration
		var double heightDueToAcceleration

		new(PathMoment initialMoment, double firstTickDuration, double tickDuration, double finalTickTime) {
			super(initialMoment, firstTickDuration, tickDuration, finalTickTime)
		}

		override setTickDuration(double tickDuration) {
			this.velocityDueToAcceleration = this.getCurrentAcceleration() * tickDuration
			this.heightDueToAcceleration = this.velocityDueToAcceleration * tickDuration / 2.0
		}

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
		return new ConstantAccelerationMomentIterator(this.initialMoment, firstTickDuration, tickDuration,
			this.finalTime)
	}

	override ConstantAccelerationPathLeg doTruncate(double newInitialTime) {
		Preconditions.checkArgument( this.initialTime <= newInitialTime );
		Preconditions.checkArgument( newInitialTime <= this.finalTime);

		return new ConstantAccelerationPathLeg(
			new ConstantAccelerationMomentIterator(this.initialMoment, newInitialTime - this.initialMoment.time,
				this.finalTime - newInitialTime, this.finalTime).next(),
			this.finalTime - newInitialTime
		)
	}
}
