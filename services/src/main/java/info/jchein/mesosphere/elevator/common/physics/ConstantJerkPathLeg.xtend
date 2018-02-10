package info.jchein.mesosphere.elevator.common.physics

import org.eclipse.xtend.lib.annotations.Data
import com.google.common.base.Preconditions

@Data
class ConstantJerkPathLeg extends AbstractPathLeg {
	override DisplacementFormulaType getDisplacementFormulaType() {
		return DisplacementFormulaType.NON_ZERO_JERK
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

	/**
	 * Encapsulate the math for incrementing a PathMoment perpetually through constant jerk.  This allows it to be implemented one time
	 * and then reused in the ConstantJerkIterator to iterate once with a unique "first time" offset, then create a second instance for
	 * reuse during the duration-bound iteration phase.  Previously this was enapsulated in the iterator itself, but that meant the logic
	 * had to be implemented correctly twice in order to account for starting the sequence of moments at any arbitrary point along the leg.
	 * 
	 * Supporting arbitrary starting points was a necessity because the PathLeg subunits that link end-to-end in order to define a JourneyArc
	 * do not necessarily exist in even multiples of the tick duration.  In order to jump from any PathLeg to the next, it must be possible
	 * to deduct the duration of the previous PathLeg's last PathMoment from the tick duration, then use that value as an initial offset 
	 * for the first PathMoment of the next PathLeg's sequence.
	 */
	private static class ConstantJerkMomentIterator extends AbstractPathLegMomentIterator {
		var double tickDegreeOne  // For use when calculating next position or next velocity
		var double tickDegreeTwo  // For use when calculating next position

		var double accelerationDueToJerk
		var double velocityDueToJerk
		var double heightDueToJerk
		var double jerk

		new(PathMoment initialMoment, double initialTickDuration, double tickDuration, double finalTime) {
			super(initialMoment, initialTickDuration, tickDuration, finalTime)
			this.jerk = initialMoment.jerk
//			if (this.hasNext()) {
//				this.tickDuration = tickDuration
//			} else {
				// In this case, we will only ever attempt to read this.pm, but perform explicit
				// initialization of the remaining state without wasting any computation on producing
				// values that will never be read.
//				this.tickDegreeOne = 0
//				this.tickDegreeTwo = 0
//				this.accelerationDueToJerk = 0
//				this.velocityDueToJerk = 0
//				this.heightDueToJerk = 0
//			}
		}
		
		override void setTickDuration(double tickDuration) {
			this.tickDegreeOne = tickDuration
			this.tickDegreeTwo = tickDuration * tickDuration / 2.0

			this.accelerationDueToJerk = tickDuration * this.jerk
			this.velocityDueToJerk = this.accelerationDueToJerk * tickDuration / 2.0
			this.heightDueToJerk = this.velocityDueToJerk * tickDuration / 3.0
		}
		
		override double getNextAcceleration() {
			return this.currentAcceleration + this.accelerationDueToJerk
		}
		
		override double getNextVelocity() {
			return this.currentVelocity + (this.currentAcceleration * this.tickDegreeOne) + this.velocityDueToJerk
		}
			
		override double getNextHeight() {
			return this.currentHeight + (this.currentVelocity * this.tickDegreeOne) + (this.currentAcceleration * this.tickDegreeTwo) + this.heightDueToJerk
		}
	}

	override ConstantJerkMomentIterator getMomentIterator(double firstTickDuration, double tickDuration) {
		return new ConstantJerkMomentIterator(this.initialMoment, firstTickDuration, tickDuration, this.finalTime)
	}
	
	override ConstantJerkPathLeg doTruncate(double newInitialTime) {
		Preconditions.checkArgument( this.initialTime <= newInitialTime );
		Preconditions.checkArgument( newInitialTime <= this.finalTime);

		return new ConstantJerkPathLeg(
			new ConstantJerkMomentIterator(this.initialMoment, newInitialTime - this.initialMoment.time,
				this.finalTime - newInitialTime, this.finalTime).next(),
			this.finalTime - newInitialTime
		)
	}
	
}
