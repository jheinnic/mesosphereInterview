package info.jchein.mesosphere.elevator.common.physics

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableList
import de.oehme.xtend.contrib.Cached
import info.jchein.mesosphere.elevator.common.DirectionOfTravel
import info.jchein.mesosphere.elevator.common.bootstrap.BuildingDescription
import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentConfiguration
import info.jchein.mesosphere.elevator.common.bootstrap.DoorTimeDescription
import info.jchein.mesosphere.elevator.common.bootstrap.StartStopDescription
import info.jchein.mesosphere.elevator.common.bootstrap.TravelSpeedDescription
import info.jchein.mesosphere.elevator.common.bootstrap.WeightDescription
import org.eclipse.xtend.lib.annotations.ToString
import org.springframework.stereotype.Component
import org.springframework.util.Assert

import static extension java.lang.Math.floor
import org.springframework.context.annotation.Scope
import org.springframework.beans.factory.config.BeanDefinition

@ToString
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
class ElevatorPhysicsService implements IElevatorPhysicsService {
	val BuildingDescription buildingProps
	val StartStopDescription motorProps
	val TravelSpeedDescription speedProps
	val DoorTimeDescription doorProps
	val WeightDescription weightProps

	val int numFloors
	val double metersPerFloor
	val double maxJerk
	val double minJerk
	val double maxAccel
	val double minAccel

	val double distBrk
	val double upSpeedBrk
	val double downSpeedBrk

	val double tMaxA
	val double vMaxA
	val double dMaxA

	val double tStopBrk
	val double maxAccelBrake
	val double minAccelBrake

	val int minFastDistance

	val JourneyArc slowAscent
	val JourneyArc slowDescent
	val JourneyArc fastAscent
	val JourneyArc fastDescent

	public new(DeploymentConfiguration deploymentConfiguration) {
		this.buildingProps = deploymentConfiguration.building
		this.motorProps = deploymentConfiguration.motor
		this.speedProps = deploymentConfiguration.topSpeed
		this.doorProps = deploymentConfiguration.doors
		this.weightProps = deploymentConfiguration.weight

		this.numFloors = this.buildingProps.getNumFloors
		this.metersPerFloor = this.buildingProps.getMetersPerFloor
		this.maxJerk = this.motorProps.maxJerk
		this.minJerk = -1 * this.maxJerk
		this.maxAccel = this.motorProps.maxAcceleration
		this.minAccel = -1 * this.maxAccel
		this.upSpeedBrk = this.motorProps.brakeSpeed
		this.downSpeedBrk = -1 * this.upSpeedBrk
		this.distBrk = this.motorProps.brakeDistance

		// Compute the time required to reach maximum acceleration, given maximum jerk, the resulting velocity, and the
		// distance traveled in that time.
		this.tMaxA = maxAccel / maxJerk;
		this.vMaxA = maxJerk * tMaxA * tMaxA / 2.0;
		this.dMaxA = vMaxA * tMaxA / 3.0;

		// Compute the deceleration required to stop from braking speed in the given stopping distance at constant
		// deceleration.  Derive the time to complete a stop from the braking distance marker using that acceleration
		// rate and confirm that it requires less than the maximum acceleration magnitude to stop from braking speed at the
		// given distance.
		//
		// s = s0 + v0*t + (0.5)*a*t^2
		// v = v0 + a*t
		// v^2 = v0^2 + 2a(s - s0)
		this.maxAccelBrake = this.upSpeedBrk * this.upSpeedBrk / (2 * this.distBrk)
		this.tStopBrk = this.upSpeedBrk / this.maxAccelBrake
		this.minAccelBrake = -1 * this.maxAccelBrake

		Preconditions.checkArgument(
			this.maxAccelBrake < maxAccel,
			"It must be possible to brake from the stopping distance at constant rate of velocity change"
		);
		Preconditions.checkArgument(
			this.minAccelBrake > minAccel,
			"It must be possible to brake from the stopping distance at constant rate of velocity change"
		);

//		this.upwardArcs = newArrayOfSize(this.numFloors - 1);
//		this.downwardArcs = newArrayOfSize(this.numFloors - 1);
		val double floorHeight = this.metersPerFloor
		val int lastFloorPair = this.numFloors - 1;
		val double buildingHeight = floorHeight * lastFloorPair;

		// Construct the shortest unit paths for each combniation of direction and speed.
		this.slowAscent = this.computeUpwardArch(PathMoment.build [
			it.time(0).height(0).velocity(0).acceleration(0).jerk(0)
		], this.speedProps.shortHop)
		this.slowDescent = this.computeDownwardArch(PathMoment.build [
			it.time(0).height(buildingHeight).velocity(0).acceleration(0).jerk(0)
		], -1 * this.speedProps.shortHop)
		this.fastAscent = this.computeUpwardArch(PathMoment.build [
			it.time(0).height(0).velocity(0).acceleration(0).jerk(0)
		], this.speedProps.longAscent)
		this.fastDescent = this.computeDownwardArch(PathMoment.build [
			it.time(0).height(buildingHeight).velocity(0).acceleration(0).jerk(0)
		], this.speedProps.longDescent)

		for (nextLeg : this.slowAscent) { System.out.println(nextLeg.toString()); }
            
		Assert.isTrue(slowDescent.shortestPossibleArc <= floorHeight,
			"Must be able to traverse one floor within slow speed arc's path")

		var ii = 0
		var nextHeight = floorHeight
		var fastArcDistance = fastAscent.shortestPossibleArc

		for (; ii < lastFloorPair && nextHeight < fastArcDistance; ii++) {
			System.out.println(String.format("%d is slow", ii));
			nextHeight += floorHeight
		}

		this.minFastDistance = ii + 1

		for (var jj = ii; jj < lastFloorPair; jj++) {
			System.out.println(String.format("%d is fast", jj));
			nextHeight += floorHeight
		}
	}

	private def PathMoment nextMoment(IPathLeg pathLeg, ImmutableList.Builder<IPathLeg> listBuilder, double nextJerk) {
		listBuilder.add(pathLeg);
		return pathLeg.getFinalMoment(nextJerk)
	}

	private def ImmutableList<IPathLeg> endPath(IPathLeg finalLeg, ImmutableList.Builder<IPathLeg> listBuilder) {
		listBuilder.add(finalLeg)
		return listBuilder.build();
	}

	/**
	 * As long as the elevator reaches maximum velocity during a given journey, a calculation of its travel time will
	 * be the sum of two segments.  The segment where its speed is ramping up and then slowing to a stop has constant
	 * duration, but the other segment, consisting of a variable length run at constant velocity, is dependent on the
	 * total travel distance.
	 * 
	 * This function calculates the fixed portions for a given maximum velocity.  Those value are then re-used to populate
	 * an array of pre-computed results starting from the longest traveled path and proceeding towards shorter paths.
	 * 
	 * When the computed values fall into a range where the car no longer has any sustained travel time at its maximum
	 * velocity, this function gets called again to recompute the acceleration/deceleration time spans a few additional
	 * times for the unique travel distances that require distinctly unique values.
	 * 
	 * Compute the time required to reach maximum acceleration, given maximum jerk, the resulting velocity, and the
	 * distance traveled in that time.
	 * 
	 * The time to reduce acceleration from max to 0 is the same as needed to increase it from 0 to max.  Account
	 * for an interval required at negative constant jerk to switch from constant acceleration to constant and maximal
	 * velocity.  
	 * 
	 * Compute the velocity when this reduction begins in order to achieve and sustain stated maximum speeds without
	 * exceeding maximum jerk when we eventually have to reduce the acceleration towards 0.  For floors with enough
	 * distance between them to support reaching maximum speed, the remaining distance following the pre-computed
	 * intervals from this block and the two that follow will be accounted for in constant velocity at maximum speed.
	 * 
	 * vf = v0 + a0*t + jt^2/2 = v0 + (tMaxA * aMax) + (vMaxA)
	 * 
	 * Let vf be the known maximum speed, a0 be the known maximum acceleration, and j be negative maximmal jerk, because
	 * we will be decelerating at this stage.  Note that we want to subtract negative jerk, so we can just add the
	 * stored positive value equivalently.
	 * 
	 * v0 = vf - a0*t - jt^2/2 
	 * 
	 * Now that we know the velocity where constant acceleration begins tapering off, compute the time it takes to
	 * reach that speed at constant acceleration, and the distance traveled in that time.  This interval is computed
	 * after the transition from constant acceleration to constant velocity, but it occurs before that interval.  We
	 * need to proceed this way because we need to know the velocity moment that ends the constant acceleration interval
	 * before we can compute its duration in time and space.
	 * 
	 * Create a placeholder for the moment at constant velocity, then decelerate to maximum deceleration.
	 * 
	 * We travel at constant deceleration to the braking distance point, at which time we want to be traveling at
	 * the braking velocity and constant deceleration.  This requires knowing the time required to reach the target
	 * deceleration and working backwards from there to find the target velocity to jerk up from, so we can work
	 * backwards a second time to find the time duration at constant deceleration required to get there.	
	 * 
	 * The remaining interval accounts for a transition from constant maximum velocity to slow down to braking speed
	 * before we reach the final braking distance zone.  This computation also serves to inform the greatest floor 
	 * distance from which we can safely accept a destination change to an earlier floor along the same trajectory.
	 */
	private def JourneyArc computeUpwardArch(PathMoment moment, double _maxSpeed) {
		val listBuilder = ImmutableList.<IPathLeg>builder()
		val maxSpeed = if (_maxSpeed > 0) { _maxSpeed } else { -1 * _maxSpeed }

		var tJerkUpOne = (this.maxAccel - moment.acceleration) / this.maxJerk;
		var toMaxUpAcc = new ConstantJerkPathLeg( moment.copy[jerk(this.maxJerk)], tJerkUpOne)
	
	    // Determine if this is a curve that will go from positive jerk directly to negative jerk, or whether it will rise, plateau, and then revert to
	    // negative jerk.  If the former is the case, then inverting the jerk until we return to zero acceleration will yield a velocity that exceeds our
	    // maximum.
		val double vAtZeroAcc = toMaxUpAcc.finalVelocity + (this.maxAccel * this.tMaxA) + (this.minJerk * this.tMaxA * this.tMaxA / 2.0)

		// The next block picks one of two arc shapes.  Both arcs terminate at a PathMoment named "atJerkUpTwo", so the braking region that both variants
		// share can be appended with common code.
		var ConstantVelocityPathLeg toJerkDownTwo;
		if (vAtZeroAcc > maxSpeed) {
			// Proceed by calculating the time reach an equilibrium point of constant velocity at our maximum velocity.  In this case all but one of the 
			// exponential terms involing jerk cancel each other out, and we are left with tmid = Math.sqrt(v(max) / j).  In thie case, t(mid) gives us
			// the duration of both the negative and positive jerk regions that we need to be left with the elevator travelling at constant velocity equal
			// in magnitude to its maximum speed value.
			//
			// The next jerk is set here to zero, not maxJerk, because the constraints of the required zero-length constant acceleration region require
			// it.  We set maxJerk at the end of that region, which works fine since it is and always will be zero-duration in length.
			tJerkUpOne = Math.sqrt(maxSpeed / this.maxJerk)
			toMaxUpAcc = new ConstantJerkPathLeg(moment.copy[jerk(this.maxJerk)], tJerkUpOne)
			val PathMoment atMaxUpAcc = toMaxUpAcc.nextMoment(listBuilder, 0);

			// Inject a blank placeholder for first unreached constant acceleration region
			val IPathLeg toJerkDownOne = new ConstantAccelerationPathLeg(atMaxUpAcc, 0)
			val PathMoment atJerkDownOne = toJerkDownOne.nextMoment(listBuilder, this.minJerk)

			// Reuse the duration of the first jerk down for a jerk up to cosntant velocity.
			val IPathLeg toConstV = new ConstantJerkPathLeg(atJerkDownOne, tJerkUpOne)
			val PathMoment atConstV = toConstV.nextMoment(listBuilder, 0)

            // Run at constant velocity for an instant.  This segment is extensible to run longer arcs at the same velocity.
			toJerkDownTwo = new ConstantVelocityPathLeg(atConstV, 0)
		} else {
			val PathMoment atMaxUpAcc = toMaxUpAcc.nextMoment(listBuilder, 0);
			Assert.isTrue(atMaxUpAcc.acceleration == this.maxAccel && atMaxUpAcc.velocity > 0,
				"Acceleration must reach maximum");

			val tJerkDownOne = atMaxUpAcc.acceleration / this.maxJerk
			val vJerkDownOne = maxSpeed - (tJerkDownOne * atMaxUpAcc.acceleration) -
				(this.minJerk * tJerkDownOne * tJerkDownOne / 2.0)
			val tMaxUpAcc = (vJerkDownOne - atMaxUpAcc.velocity) / atMaxUpAcc.acceleration

			val toJerkDownOne = new ConstantAccelerationPathLeg(atMaxUpAcc, tMaxUpAcc)
			val atJerkDownOne = toJerkDownOne.nextMoment(listBuilder, this.minJerk)

			val toConstV = new ConstantJerkPathLeg(atJerkDownOne, tJerkDownOne)
			val atConstV = toConstV.nextMoment(listBuilder, 0)

			toJerkDownTwo = new ConstantVelocityPathLeg(atConstV, 0)
		}

		// Reverse the paired jerk maneuvers to brake down towards braking speed, then from braking speed to rest.

		// First, compute the initial acceleration required to be able to decelerate from the braking speed in the braking distance.  This will also
		// require calculating the required jerk and the duration it needs to be applied.  Use this result as target acceleration in the next step, 
		// working backwards to get to braking speed from travelling speed.
		// sf = s0 + v0*t + (a0*t^2)/2 + (j*t^3)/6
		// vf = v0 + a0*t + (j*t^2)/2
		// af = a0 + j*t
		// s0 = 0, sf = d(brk)
		// v0 = v(brk), vf = 0
		// a0 = a(min), af = 0
		// a0 = -j*t
		// v0 = 0 - a0*t - (j*t^2)/2
		// v0 = (j*t^2) - (j*t^2)/2 = (j*t^2)/2
		// d(brk) = 0 + ((j*t^2)/2)*t + (-j*t)*((t^2)/2) + (j*t^3)/6
		// d(brk) = ((j*t^3)/2) - ((j*t^3)/2) + (j*t^3)/6 = (j*t^3)/6
		// d(brk) = (v0*t)/3
		// t = 3 * d(brk) / v0
		// j = 6 * d(brk) / t^3
		// a0 = -j * t

		val double tJerkUpTwo = 3 * this.distBrk / this.upSpeedBrk;
		val double jerkUpTwo = 6 * this.distBrk / tJerkUpTwo / tJerkUpTwo / tJerkUpTwo;
		val double aJerkDownTwo = -1 * jerkUpTwo * tJerkUpTwo

		// Working backwards from
		// a(sb) = a(0) + j*t(toBrk)
		// j*t = a(sb) - a(0)
		// v(sb) = v(0) + a(0)*t + (j*t^2)/2.0
		// v(sb) - v(0) = t*a(0) + (t*a(sb) - t*a(0))/2.0
		// t = ((v(sb) - v(0)) / (2*a(0) + a(sb) - a(0))/2.0
		// t = ((v(sb) - v(0)) * 2.0) / ( a(0) + a(sb) )
		// ... yields ...
		// t(toBrk) = 2(v(brk) - v(0)) / (a(0) + a(brk))
		
		// Next, given the current velocity, braking velocity, and target acceleration, compute the time required to make that transition, and then
		// derive the required jerk.  Presume that we will not exceed maximum jerk since we accelerated to the current speed from rest without doing
		// so and are now decelerating to a velocity that is greater than 0 and in the same direction.
		val double tJerkDownTwo = (this.upSpeedBrk - toJerkDownTwo.finalVelocity) * 2 / aJerkDownTwo
		val double jerkDownTwo  = aJerkDownTwo / tJerkDownTwo

		val PathMoment atJerkDownTwo = toJerkDownTwo.nextMoment(listBuilder, jerkDownTwo)
		val toBrakes = new ConstantJerkPathLeg(atJerkDownTwo, tJerkDownTwo)
		val atBrakes = toBrakes.nextMoment(listBuilder, jerkUpTwo)

		System.out.println(String.format("%f %f", tJerkUpTwo, this.tStopBrk));
		return JourneyArc.fromList(
			new ConstantJerkPathLeg(atBrakes, tJerkUpTwo).endPath(listBuilder)
		)
	}

	private def JourneyArc computeDownwardArch(PathMoment moment, double _minSpeed) {
		val ImmutableList.Builder<IPathLeg> listBuilder = ImmutableList.<IPathLeg>builder()
		val double minSpeed = if (_minSpeed < 0) { _minSpeed } else { -1 * _minSpeed }

		var double tJerkDownOne = (this.minAccel - moment.acceleration) / this.minJerk;
		var IPathLeg toMaxDownAcc = new ConstantJerkPathLeg( moment.copy[jerk(this.minJerk)], tJerkDownOne)
	
	    // Determine if this is a curve that will go from positive jerk directly to negative jerk, or whether it will rise, plateau, and then revert to
	    // negative jerk.  If the former is the case, then inverting the jerk until we return to zero acceleration will yield a velocity that exceeds our
	    // maximum.
		val double vAtZeroAcc = toMaxDownAcc.finalVelocity + (this.minAccel * this.tMaxA) + (this.maxJerk * this.tMaxA * this.tMaxA / 2.0)

		// The next block picks one of two arc shapes.  Both arcs terminate at a PathMoment named "atJerkUpTwo", so the braking region that both variants
		// share can be appended with common code.
		var ConstantVelocityPathLeg toJerkUpTwo;
		if (vAtZeroAcc < minSpeed) {
			// Proceed by calculating the time reach an equilibrium point of constant velocity at our maximum velocity.  In this case all but one of the 
			// exponential terms involing jerk cancel each other out, and we are left with tmid = Math.sqrt(v(max) / j).  In thie case, t(mid) gives us
			// the duration of both the negative and positive jerk regions that we need to be left with the elevator travelling at constant velocity equal
			// in magnitude to its maximum speed value.
			//
			// The next jerk is set here to zero, not maxJerk, because the constraints of the required zero-length constant acceleration region require
			// it.  We set maxJerk at the end of that region, which works fine since it is and always will be zero-duration in length.
			tJerkDownOne = Math.sqrt(minSpeed / this.minJerk)
			toMaxDownAcc = new ConstantJerkPathLeg(moment.copy[jerk(this.minJerk)], tJerkDownOne)
			val PathMoment atMaxDownAcc = toMaxDownAcc.nextMoment(listBuilder, 0);

			// Inject a blank placeholder for first unreached constant acceleration region
			val IPathLeg toJerkUpOne = new ConstantAccelerationPathLeg(atMaxDownAcc, 0)
			val PathMoment atJerkUpOne = toJerkUpOne.nextMoment(listBuilder, this.maxJerk)

			// Reuse the duration of the first jerk down for a jerk up to cosntant velocity.
			val IPathLeg toConstV = new ConstantJerkPathLeg(atJerkUpOne, tJerkDownOne)
			val PathMoment atConstV = toConstV.nextMoment(listBuilder, 0)

            // Run at constant velocity for an instant.  This segment is extensible to run longer arcs at the same velocity.
			toJerkUpTwo = new ConstantVelocityPathLeg(atConstV, 0)
		} else {
			val PathMoment atMaxDownAcc = toMaxDownAcc.nextMoment(listBuilder, 0);

			val double tJerkUpOne = atMaxDownAcc.acceleration / this.minJerk
			val double vJerkUpOne = minSpeed - (tJerkUpOne * atMaxDownAcc.acceleration) -
				(this.maxJerk * tJerkUpOne * tJerkUpOne / 2.0)
			val tMaxDownAcc = (vJerkUpOne - atMaxDownAcc.velocity) / atMaxDownAcc.acceleration
	
			val IPathLeg toJerkUpOne = new ConstantAccelerationPathLeg(atMaxDownAcc, tMaxDownAcc)
			val PathMoment atJerkUpOne = toJerkUpOne.nextMoment(listBuilder, this.maxJerk)
	
			val IPathLeg toConstV = new ConstantJerkPathLeg(atJerkUpOne, tJerkUpOne)
			val PathMoment atConstV = toConstV.nextMoment(listBuilder, 0)
	
			toJerkUpTwo = new ConstantVelocityPathLeg(atConstV, 0)
		}

		// Reverse the paired jerk maneuvers to brake down towards braking speed, then from braking speed to rest.

		// First, compute the initial acceleration required to be able to decelerate from the braking speed in the braking distance.  This will also
		// require calculating the required jerk and the duration it needs to be applied.  Use this result as target acceleration in the next step, 
		// working backwards to get to braking speed from travelling speed.
		// sf = s0 + v0*t + (a0*t^2)/2 + (j*t^3)/6
		// vf = v0 + a0*t + (j*t^2)/2
		// af = a0 + j*t
		// s0 = 0, sf = d(brk)
		// v0 = v(brk), vf = 0
		// a0 = a(min), af = 0
		// a0 = -j*t
		// v0 = 0 - a0*t - (j*t^2)/2
		// v0 = (j*t^2) - (j*t^2)/2 = (j*t^2)/2
		// d(brk) = 0 + ((j*t^2)/2)*t + (-j*t)*((t^2)/2) + (j*t^3)/6
		// d(brk) = ((j*t^3)/2) - ((j*t^3)/2) + (j*t^3)/6 = (j*t^3)/6
		// d(brk) = (v0*t)/3
		// t = 3 * d(brk) / v0
		// j = 6 * d(brk) / t^3
		// a0 = -j * t

		val double tJerkDownTwo = -3 * this.distBrk / this.downSpeedBrk;
		val double jerkDownTwo = -6 * this.distBrk / tJerkDownTwo / tJerkDownTwo / tJerkDownTwo;
		val double aJerkUpTwo = -1 * jerkDownTwo * tJerkDownTwo

		// Working backwards from
		// a(sb) = a(0) + j*t(toBrk)
		// j*t = a(sb) - a(0)
		// v(sb) = v(0) + a(0)*t + (j*t^2)/2.0
		// v(sb) - v(0) = t*a(0) + (t*a(sb) - t*a(0))/2.0
		// t = ((v(sb) - v(0)) / (2*a(0) + a(sb) - a(0))/2.0
		// t = ((v(sb) - v(0)) * 2.0) / ( a(0) + a(sb) )
		// ... yields ...
		// t(toBrk) = 2(v(brk) - v(0)) / (a(0) + a(brk))
		
		// Next, given the current velocity, braking velocity, and target acceleration, compute the time required to make that transition, and then
		// derive the required jerk.  Presume that we will not exceed maximum jerk since we accelerated to the current speed from rest without doing
		// so and are now decelerating to a velocity that is greater than 0 and in the same direction.
		val double tJerkUpTwo = (this.downSpeedBrk - toJerkUpTwo.finalVelocity) * 2 / aJerkUpTwo
		val double jerkUpTwo  = aJerkUpTwo / tJerkUpTwo

		val PathMoment atJerkUpTwo = toJerkUpTwo.nextMoment(listBuilder, jerkUpTwo)
		val toBrakes = new ConstantJerkPathLeg(atJerkUpTwo, tJerkUpTwo)
		val atBrakes = toBrakes.nextMoment(listBuilder, jerkDownTwo)

		System.out.println(String.format("%f %f", tJerkDownTwo, this.tStopBrk));
		return JourneyArc.fromList(
			new ConstantJerkPathLeg(atBrakes, tJerkDownTwo).endPath(listBuilder)
		)
	}

	override isTravelFast(int fromFloorIndex, int toFloorIndex) {
		return if (fromFloorIndex > toFloorIndex) {
			((fromFloorIndex - toFloorIndex) >= this.minFastDistance)
		} else if (fromFloorIndex < toFloorIndex) {
			((toFloorIndex - fromFloorIndex) >= this.minFastDistance)
		} else {
			false
		}
	}

	override getExpectedStopDuration(int boardingCount, int disembarkingCount) {
		return this.doorProps.openCloseTime + Math.max(this.doorProps.minHold,
			(this.doorProps.personHold * (boardingCount + disembarkingCount)));
	}

	override getIdealPassengerCount() {
		return ((
			this.weightProps.maxForTravel * this.weightProps.pctMaxForIdeal
		) / this.weightProps.avgPassenger).floor() as int
	}

	override getMaxTolerancePassengerCount() {
		return ((
			this.weightProps.maxForTravel * this.weightProps.pctMaxForPickup
		) / this.weightProps.avgPassenger).floor() as int
	}

	override getFloorDistance(int fromFloorIndex, int toFloorIndex) {
		if (toFloorIndex > fromFloorIndex) {
			return (toFloorIndex - fromFloorIndex) * this.buildingProps.getMetersPerFloor
		} else {
			return (fromFloorIndex - toFloorIndex) * this.buildingProps.getMetersPerFloor
		}
	}

	override double getTravelTime(int fromFloorIndex, int toFloorIndex) {
		Preconditions.checkArgument(fromFloorIndex != toFloorIndex,
			"to and from floor indices cannot both be <%s> and <%s>", fromFloorIndex, toFloorIndex);

		return this.doGetTraversalPath(fromFloorIndex, toFloorIndex).getDuration();
	}

	def double travelDuration(ImmutableList<IPathLeg> path) {
		return path.get(path.size() - 1).finalTime - path.get(0).initialTime
	}

	def double travelDistance(ImmutableList<IPathLeg> path) {
		val displacement = path.get(path.size() - 1).finalHeight - path.get(0).initialHeight

		return if (displacement < 0) {
			displacement * -1
		} else {
			displacement
		}
	}

	@Cached
	def JourneyArc doGetTraversalPath(int fromFloorIndex, int toFloorIndex) {
		val boolean fastTravel = this.isTravelFast(fromFloorIndex, toFloorIndex)
		val double absoluteDistance = this.getFloorDistance(fromFloorIndex, toFloorIndex)
		var double pathShift = 0
		
		val JourneyArc baseOriginPath = 
			if (fromFloorIndex < toFloorIndex) {
				pathShift = fromFloorIndex * this.metersPerFloor

				if (fastTravel) {
					this.fastAscent
				} else {
					this.slowAscent
				}
			} else {
				pathShift = (1 - this.numFloors + fromFloorIndex) * this.metersPerFloor

				if (fastTravel) {
					this.fastDescent
				} else {
					this.slowDescent
				}
			}
//					this.computeUpwardArch(
//						PathMoment.build[
//							time(0).height(fromFloorIndex * this.buildingProps.getMetersPerFloor).velocity(0).acceleration(0).jerk(0)
//						], this.speedProps.longAscent
//					)
//				} else {
//					this.computeUpwardArch(
//						PathMoment.build[
//							time(0).height(fromFloorIndex * this.buildingProps.getMetersPerFloor).velocity(0).acceleration(0).jerk(0)
//						], this.speedProps.shortHop
//					)
//				}
//			} else if (this.isTravelFast(fromFloorIndex, toFloorIndex)) { 
//					this.computeDownwardArch(
//						PathMoment.build[
//							time(0).height(fromFloorIndex * this.buildingProps.getMetersPerFloor).velocity(0).acceleration(0).jerk(0)
//						], this.speedProps.longDescent
//					)
//				} else {
//					this.computeDownwardArch(
//						PathMoment.build[
//							time(0).height(fromFloorIndex * this.buildingProps.getMetersPerFloor).velocity(0).acceleration(0).jerk(0)
//						], this.speedProps.shortHop
//					)
//				}
//			}
		// System.out.println(String.format("%d to %d targets a distance of %f", fromFloorIndex, toFloorIndex, this.floorDistance(fromFloorIndex, toFloorIndex)));
		
		// Having selected the baseline shortest path, actual path distance, and an endpoint shift, compute a suitable derived path
		return baseOriginPath.adjustConstantRegion(absoluteDistance).moveEndpointsByOffset(pathShift)
	}

	override getTraversalPath(int fromFloorIndex, int toFloorIndex) {
		return this.doGetTraversalPath(fromFloorIndex, toFloorIndex)
	}

	override getNumElevators() {
		return this.numElevators
	}

	override getNumFloors() {
		return this.numFloors
	}

	override getMetersPerFloor() {
		return this.metersPerFloor
	}
	
	override getSensorHeight(int floorIndex, DirectionOfTravel direction) {
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}
	
}
