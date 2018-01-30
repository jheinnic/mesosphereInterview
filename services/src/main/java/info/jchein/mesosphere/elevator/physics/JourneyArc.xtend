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
		Preconditions.checkArgument(newDistance >= minDistance, "Cannot produce %s, an arc shorter than %s", newDistance, minDistance)

		var double tempDeltaHeight = 0.0
		if (direction == DirectionOfTravel.GOING_UP) {
			tempDeltaHeight = newDistance - terminalMoment.height + initialMoment.height
		} else { 
			tempDeltaHeight = initialMoment.height - terminalMoment.height - newDistance
		}
		val double deltaHeight = tempDeltaHeight
		val double deltaCVDuration = deltaHeight / this.constantVelocity.finalVelocity
		val double newConstantDuration = this.constantVelocity.duration + deltaCVDuration
		
		// System.out.println(String.format("deltaHeight = %f, deltaDuration = %f, newDuration = %f", deltaHeight, deltaCVDuration, newConstantDuration));
		
		return this.copy [ bldr |
			bldr.constantVelocity(
				this.constantVelocity.withNewDuration(newConstantDuration)
			).reverseJerkTwo(
				this.reverseJerkTwo.withSpaceTimeShift(deltaHeight, deltaCVDuration)
			).reverseAcceleration(
				this.reverseAcceleration.withSpaceTimeShift(deltaHeight, deltaCVDuration)
			).forwardJerkTwo(
				this.forwardJerkTwo.withSpaceTimeShift(deltaHeight, deltaCVDuration)
			).terminalSegment(
				this.terminalSegment.withSpaceTimeShift(deltaHeight, deltaCVDuration)
			).terminalMoment(
				this.terminalMoment.copy[
					time(this.terminalMoment.time + deltaCVDuration)
					.height(this.terminalMoment.height + deltaHeight)
				]
			)
		]
	}
	
	def JourneyArc moveEndpointsByOffset(double offset) {
		this.copy [ bldr |
			bldr.initialMoment(
				this.initialMoment.withHeightOffset(offset)
			).forwardJerkOne(
				this.forwardJerkOne.withHeightOffset(offset)
			).forwardAcceleration(
				this.forwardAcceleration.withHeightOffset(offset)
			).reverseJerkOne(
				this.reverseJerkOne.withHeightOffset(offset)
			).constantVelocity(
				this.constantVelocity.withHeightOffset(offset)
			).reverseJerkTwo(
				this.reverseJerkTwo.withHeightOffset(offset)
			).reverseAcceleration(
				this.reverseAcceleration.withHeightOffset(offset)
			).forwardJerkTwo(
				this.forwardJerkTwo.withHeightOffset(offset)
			).terminalSegment(
				this.terminalSegment.withHeightOffset(offset)
			).terminalMoment(
				this.terminalMoment.withHeightOffset(offset)
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
	
	private def ConstantJerkPathLeg withSpaceTimeShift(
		ConstantJerkPathLeg leg, double deltaHeight, double deltaTime
	) {
		new ConstantJerkPathLeg(
			leg.initialMoment.copy[
				time(leg.initialTime + deltaTime)
				.height(leg.initialHeight + deltaHeight) 
			], leg.duration
		)
	}
	
	private def ConstantAccelerationPathLeg withSpaceTimeShift(
		ConstantAccelerationPathLeg leg, double deltaHeight, double deltaTime
	) {
		new ConstantAccelerationPathLeg(
			leg.initialMoment.copy[
				time(leg.initialTime + deltaTime)
				.height(leg.initialHeight + deltaHeight) 
			], leg.duration
		)
	}
	
	private def ConstantJerkPathLeg withHeightOffset(ConstantJerkPathLeg leg, double heightOffset) {
		return new ConstantJerkPathLeg(
			leg.initialMoment.withHeightOffset(heightOffset), leg.duration
		)
	}
	
	private def ConstantAccelerationPathLeg withHeightOffset(ConstantAccelerationPathLeg leg, double heightOffset) {
		return new ConstantAccelerationPathLeg(
			leg.initialMoment.withHeightOffset(heightOffset), leg.duration
		)
	}
	
	private def ConstantVelocityPathLeg withHeightOffset(ConstantVelocityPathLeg leg, double heightOffset) {
		return new ConstantVelocityPathLeg(
			leg.initialMoment.withHeightOffset(heightOffset), leg.duration
		)
	}
	
	private def PathMoment withHeightOffset(PathMoment initialMoment, double shiftOffset) {
		return initialMoment.copy[ height(initialMoment.height + shiftOffset) ]
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
