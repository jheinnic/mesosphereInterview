package info.jchein.mesosphere.elevator.common.physics

import com.google.common.base.Preconditions
import de.oehme.xtend.contrib.Buildable
import info.jchein.mesosphere.elevator.common.DirectionOfTravel
import java.util.List
import org.eclipse.xtend.lib.annotations.Data

@Data
@Buildable
class JourneyArc implements Iterable<IPathLeg>
{
	val PathMoment initialMoment
	val PathMoment terminalMoment
	val ConstantJerkPathLeg forwardJerkOne
	val ConstantAccelerationPathLeg forwardAcceleration
	val ConstantJerkPathLeg reverseJerkOne
	val ConstantVelocityPathLeg constantVelocity
	val ConstantJerkPathLeg reverseJerkTwo
	val ConstantAccelerationPathLeg reverseAcceleration
	val ConstantJerkPathLeg forwardJerkTwo
	val ConstantAccelerationPathLeg terminalSegment

	override iterator() {
		return new JourneyArcPathLegIterator(this)
	}
	
	def JourneyArcMomentSeries asMomentIterable(double tickDuration) {
		return new JourneyArcMomentSeries(this, this.terminalMoment, tickDuration);
	}

	def DirectionOfTravel getDirection() {
		return switch(this) {
			case (initialMoment.height < terminalMoment.height): DirectionOfTravel.GOING_UP
			case (initialMoment.height > terminalMoment.height): DirectionOfTravel.GOING_DOWN
			default: DirectionOfTravel.STOPPED
		}
	}
	
	def double getDuration() {
		return this.terminalMoment.time - this.initialMoment.time
	}
	
	def double distance() {
		return switch (this.direction) {
			case GOING_UP: terminalMoment.height - initialMoment.height
			case GOING_DOWN: initialMoment.height - terminalMoment.height
			default: 0
		}
	}

	/**
	 * Adjusts the absolute value of distance travelled during the constant velocity region of this arc to a new value.  Be aware that distance
	 * is expressed using its absolute value.  This is most relevant when descending, where an increase in travel distance translates to a 
	 * reduction in terminal height.
	 */
	def JourneyArc adjustConstantRegion(double newDistance) {
		this.shortestPossibleArc => [minDistance |
			Preconditions.checkArgument(newDistance >= minDistance, "Cannot produce %s, an arc shorter than %s", newDistance, minDistance)
		]

		val double deltaHeight =
			switch (this.direction) {
				case GOING_UP: newDistance - this.distance()
				case GOING_DOWN: this.distance() - newDistance
				default: 0
			}
		val double deltaCVDuration = deltaHeight / this.constantVelocity.finalVelocity
		val double newConstantDuration = this.constantVelocity.duration + deltaCVDuration
		
		return this.copy [ 
			constantVelocity = this.constantVelocity.withNewDuration(newConstantDuration)
			reverseJerkTwo = this.reverseJerkTwo.withSpaceTimeShift(deltaHeight, deltaCVDuration)
			reverseAcceleration = this.reverseAcceleration.withSpaceTimeShift(deltaHeight, deltaCVDuration)
			forwardJerkTwo = this.forwardJerkTwo.withSpaceTimeShift(deltaHeight, deltaCVDuration)
			terminalSegment = this.terminalSegment.withSpaceTimeShift(deltaHeight, deltaCVDuration)
			terminalMoment(
				this.terminalMoment.copy[
					time(this.terminalMoment.time + deltaCVDuration)
					height(this.terminalMoment.height + deltaHeight)
				]
			)
		]
	}
	
	def JourneyArc moveEndpointsByOffset(double offset) {
		this.copy [ 
			initialMoment = this.initialMoment.withHeightOffset(offset)
			forwardJerkOne = this.forwardJerkOne.withHeightOffset(offset)
			forwardAcceleration = this.forwardAcceleration.withHeightOffset(offset)
			reverseJerkOne = this.reverseJerkOne.withHeightOffset(offset)
			constantVelocity = this.constantVelocity.withHeightOffset(offset)
			reverseJerkTwo = this.reverseJerkTwo.withHeightOffset(offset)
			reverseAcceleration = this.reverseAcceleration.withHeightOffset(offset)
			forwardJerkTwo = this.forwardJerkTwo.withHeightOffset(offset)
			terminalSegment = this.terminalSegment.withHeightOffset(offset)
			terminalMoment = this.terminalMoment.withHeightOffset(offset)
		]
	}
	
	
	def static fromList(List<IPathLeg> list) {
		return JourneyArc.build[
			initialMoment = list.get(0).initialMoment
			forwardJerkOne = list.get(0) as ConstantJerkPathLeg
			forwardAcceleration = list.get(1) as ConstantAccelerationPathLeg
			reverseJerkOne = list.get(2) as ConstantJerkPathLeg
			constantVelocity = list.get(3) as ConstantVelocityPathLeg
			reverseJerkTwo = list.get(4) as ConstantJerkPathLeg
			reverseAcceleration = list.get(5) as ConstantAccelerationPathLeg
			forwardJerkTwo = list.get(6) as ConstantJerkPathLeg
			terminalSegment = list.get(7) as ConstantAccelerationPathLeg
			terminalMoment = list.get(7).getFinalMoment(0)
		]
	}
	
	private def ConstantJerkPathLeg withSpaceTimeShift(ConstantJerkPathLeg leg, double deltaHeight, double deltaTime)
	{
		return new ConstantJerkPathLeg(
			leg.initialMoment.copy[
				time(leg.initialTime + deltaTime)
				height(leg.initialHeight + deltaHeight)
			], leg.duration
		)
	}
	
	private def ConstantAccelerationPathLeg withSpaceTimeShift( ConstantAccelerationPathLeg leg, double deltaHeight, double deltaTime)
	{
		return new ConstantAccelerationPathLeg(
			leg.initialMoment.copy[
				time(leg.initialTime + deltaTime)
				height(leg.initialHeight + deltaHeight) 
			], leg.duration
		)
	}
	
	public def getBrakeAppliedMoment() {
		return this.forwardJerkTwo.getFinalMoment(0);
	}

	def double getShortestPossibleArc() {
		// TODO: This is confusing because JourneyArc uses absolute values to return its distance, but PathLegs do not.  When descending,
		//       we therefor have to invert the sign of the constant velocity moment's distance to deduct its absolute value from this
		//       JourneyArc's distance.  This is mind-twisting.  Refactor the consuming code to understand that "shortest arc" and "distance"
		//       metrics are negative when GOING_DOWN rather than staying mindful about which abstractions use absolute notions and which
		//       do not.
		return switch(this.direction) {
			case GOING_UP: this.distance() - constantVelocity.distance
			case GOING_DOWN: this.distance() + constantVelocity.distance
			default: 0
		}
	}
	
	private def withHeightOffset( ConstantJerkPathLeg leg, double heightOffset) {
		return new ConstantJerkPathLeg(
			leg.initialMoment.withHeightOffset(heightOffset), leg.duration
		)
	}
	
	private def withHeightOffset( ConstantAccelerationPathLeg leg, double heightOffset) {
		return new ConstantAccelerationPathLeg(
			leg.initialMoment.withHeightOffset(heightOffset), leg.duration
		)
	}
	
	private def withHeightOffset( ConstantVelocityPathLeg leg, double heightOffset) {
		return new ConstantVelocityPathLeg(
			leg.initialMoment.withHeightOffset(heightOffset), leg.duration
		)
	}
	
	private def withHeightOffset( PathMoment initialMoment, double shiftOffset) {
		return initialMoment.copy[ height(initialMoment.height + shiftOffset) ]
	}
}
