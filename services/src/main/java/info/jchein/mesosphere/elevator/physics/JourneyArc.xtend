package info.jchein.mesosphere.elevator.physics

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableList
import de.oehme.xtend.contrib.Buildable
import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel
import org.eclipse.xtend.lib.annotations.Data

@Data
@Buildable
class JourneyArc {
	val PathMoment initialMoment

	val ConstantJerkPathLeg forwardJerkOne
	val ConstantAccelerationPathLeg forwardAcceleration
	val ConstantJerkPathLeg reverseJerkOne
	val ConstantVelocityPathLeg constantVelocity
	val ConstantJerkPathLeg reverseJerkTwo
	val ConstantAccelerationPathLeg reverseAcceleration
	val ConstantJerkPathLeg forwardJerkTwo
	val ConstantAccelerationPathLeg terminalSegment
	
	val PathMoment terminalMoment

	def JourneyPathLegIterator legIterator() {
		return new JourneyPathLegIterator(this)
	}
	
	def PathMomentIterator momentIterator(double tickDuration) {
		return new PathMomentIterator(this.legIterator(), tickDuration)
	}

	def DirectionOfTravel direction() {
		return if (initialMoment.height < terminalMoment.height) {
			DirectionOfTravel.GOING_UP
		} else if (initialMoment.height > terminalMoment.height) {
			DirectionOfTravel.GOING_DOWN
		} else DirectionOfTravel.STOPPED;
	}
	
	def double duration() {
		return this.terminalMoment.time - this.initialMoment.time
	}
	
	def double distance() {
		return if (initialMoment.height < terminalMoment.height) {
			terminalMoment.height - initialMoment.height
		} else if (initialMoment.height > terminalMoment.height) {
			initialMoment.height - terminalMoment.height
		} else {
			0
		}
	}

	def JourneyArc adjustConstantRegion(double newDistance) {
		val minDistance = this.shortestPossibleArc
		Preconditions.checkArgument(newDistance >= minDistance, "Cannot produce an arc shorter than %s", minDistance)

		val deltaHeight = if (direction == DirectionOfTravel.GOING_UP) {
			newDistance - terminalMoment.height - initialMoment.height
		} else { 
			initialMoment.height - terminalMoment.height - newDistance
		}
		val deltaCVDuration = deltaHeight / this.constantVelocity.finalVelocity
		val newConstantDuration = this.constantVelocity.duration + deltaCVDuration
		
		this.copy [ bldr |
			bldr.constantVelocity(
				this.constantVelocity.withNewDuration(newConstantDuration)
			).reverseJerkTwo(
				this.reverseJerkTwo.withDeltaHeight(deltaHeight, deltaCVDuration)
			).reverseAcceleration(
				this.reverseAcceleration.withDeltaHeight(deltaHeight, deltaCVDuration)
			).forwardJerkTwo(
				this.forwardJerkTwo.withDeltaHeight(deltaHeight, deltaCVDuration)
			).terminalSegment(
				this.terminalSegment.withDeltaHeight(deltaHeight, deltaCVDuration)
			).terminalMoment(
				this.terminalMoment.copy[
					time(this.terminalMoment.time + deltaCVDuration)
					.height(this.terminalMoment.height + deltaHeight)
				]
			)
		]
	}
	
	
	def static fromList(ImmutableList<PathLeg> list) {
		return JourneyArc.build[
			it.initialMoment(
				list.get(0).initialMoment
			).forwardJerkOne(
				list.get(0) as ConstantJerkPathLeg
			).forwardAcceleration(
				list.get(1) as ConstantAccelerationPathLeg
			).reverseJerkOne(
				list.get(2) as ConstantJerkPathLeg
			).constantVelocity(
				list.get(3) as ConstantVelocityPathLeg
			).reverseJerkTwo(
				list.get(4) as ConstantJerkPathLeg
			).reverseAcceleration(
				list.get(5) as ConstantAccelerationPathLeg
			).forwardJerkTwo(
				list.get(6) as ConstantJerkPathLeg
			).terminalSegment(
				list.get(7) as ConstantAccelerationPathLeg
			).terminalMoment(
				list.get(7).getFinalMoment(0)
			)
		]
	}
	
	def ConstantJerkPathLeg withDeltaHeight(ConstantJerkPathLeg leg, double deltaHeight, double deltaTime) {
		new ConstantJerkPathLeg(
			leg.initialMoment.copy[
				time(leg.initialTime + deltaTime)
				.height(leg.initialHeight + deltaHeight) 
			], leg.duration
		)
	}
	
	def ConstantAccelerationPathLeg withDeltaHeight(ConstantAccelerationPathLeg leg, double deltaHeight, double deltaTime) {
		new ConstantAccelerationPathLeg(
			leg.initialMoment.copy[
				height(leg.initialHeight + deltaHeight) 
			], leg.duration
		)
	}

	def double getShortestPossibleArc() {
		val currentDistance = terminalMoment.height - initialMoment.height

		return if (currentDistance < 0) {
			constantVelocity.distance - currentDistance
		} else {
			currentDistance - constantVelocity.distance
		}
	}
}
