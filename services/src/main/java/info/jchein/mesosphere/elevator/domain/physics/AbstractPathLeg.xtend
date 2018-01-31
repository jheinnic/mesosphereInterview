package info.jchein.mesosphere.elevator.domain.physics

import org.eclipse.xtend.lib.annotations.Data

@Data
abstract class AbstractPathLeg implements PathLeg {
	val PathMoment initialMoment
	val double finalTime
	val double duration
	
	new(PathMoment initialMoment, double duration) {
		this.duration = duration
		this.initialMoment = initialMoment
		this.finalTime = initialMoment.time + duration
	}
	
	override double getJerk() { initialMoment.jerk }

	override double getInitialTime() { initialMoment.time }
	
	override double getInitialHeight() { initialMoment.height }

	override double getInitialVelocity() { initialMoment.velocity }

	override double getInitialAcceleration() { initialMoment.acceleration }
	
	override PathMoment getInitialMoment() { initialMoment }
	
	override PathMoment getFinalMoment(double nextJerk) {
		return PathMoment.build [
			it.time(this.finalTime)
				.height(this.finalHeight)
				.velocity(this.finalVelocity)
				.acceleration(this.finalAcceleration)
				.jerk(nextJerk);
		]
	}
}
