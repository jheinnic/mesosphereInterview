package info.jchein.mesosphere.elevator.common.physics

import com.google.common.base.Preconditions
import org.eclipse.xtend.lib.annotations.Data

@Data
class ConstantVelocityPathLeg extends AbstractPathLeg {
	override DisplacementFormulaType getDisplacementFormulaType() {
		return DisplacementFormulaType.CONSTANT_VELOCITY
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

	private static class ConstantVelocityMomentIterator extends AbstractPathLegMomentIterator
	{
		var double heightDueToVelocity

		new(PathMoment moment, double firstTickDuration, double nextTickDuration, double finalTime) {
			super(moment, firstTickDuration, nextTickDuration, finalTime)
		}

		override setTickDuration(double tickDuration) {
			this.heightDueToVelocity = tickDuration * this.currentHeight
		}
		
		override double getNextAcceleration() {
			return this.currentAcceleration
		}
		
		override getNextVelocity() {
			return this.currentVelocity
		}

		override double getNextHeight() {
			return this.currentHeight + this.heightDueToVelocity
		}
	}

	override ConstantVelocityMomentIterator getMomentIterator(double firstTickDuration, double tickDuration) {
		return new ConstantVelocityMomentIterator(this.initialMoment, firstTickDuration, tickDuration, this.finalTime)
	}

	override ConstantVelocityPathLeg doTruncate(double newInitialTime) {
		Preconditions.checkArgument( this.initialTime <= newInitialTime );
		Preconditions.checkArgument( newInitialTime <= this.finalTime);

		return new ConstantVelocityPathLeg(
			new ConstantVelocityMomentIterator(this.initialMoment, newInitialTime - this.initialMoment.time,
				this.finalTime - newInitialTime, this.finalTime).next(),
			this.finalTime - newInitialTime
		);
	}
}
