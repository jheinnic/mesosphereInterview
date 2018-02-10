package info.jchein.mesosphere.elevator.common.physics

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableList
import de.oehme.xtend.contrib.Cached
import org.eclipse.xtend.lib.annotations.ToString
import org.springframework.util.Assert
import org.springframework.stereotype.Component

import info.jchein.mesosphere.elevator.runtime.IRuntimeClock
import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentConfiguration
import info.jchein.mesosphere.elevator.common.bootstrap.DoorTimeDescription
import info.jchein.mesosphere.elevator.common.bootstrap.BuildingDescription
import info.jchein.mesosphere.elevator.common.bootstrap.StartStopDescription
import info.jchein.mesosphere.elevator.common.bootstrap.TravelSpeedDescription
import info.jchein.mesosphere.elevator.common.bootstrap.WeightDescription

import static extension java.lang.Math.floor 

@ToString
@Component
class ElevatorPhysicsService implements IElevatorPhysicsService {
	val IRuntimeClock clock
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
	val double maxSpeedBrk
	val double minSpeedBrk

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

	public new(DeploymentConfiguration deploymentConfiguration, IRuntimeClock clock) {
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
		this.maxSpeedBrk = this.motorProps.brakeSpeed
		this.minSpeedBrk = -1 * this.maxSpeedBrk
		this.distBrk = this.motorProps.brakeDistance

		this.clock = clock

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
		this.maxAccelBrake = this.maxSpeedBrk * this.maxSpeedBrk / (2 * this.distBrk)
		this.tStopBrk = this.maxSpeedBrk / this.maxAccelBrake
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
		val double buildingHeight = floorHeight * this.numFloors;

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
		val maxSpeed = if (_maxSpeed > 0) {
				_maxSpeed
			} else {
				-1 * _maxSpeed
			}

		val tJerkUpOne = (this.maxAccel - moment.acceleration) / this.maxJerk;
		val toMaxUpAcc = new ConstantJerkPathLeg( moment.copy[jerk(this.maxJerk)], tJerkUpOne)
		if (toMaxUpAcc.finalVelocity > maxSpeed) {
			// 0 = (v0-v) + a0t + 0.5 * j*t^2 is technically a factor-able polynomial, but let's defer supporting this result and just insist that the motor must be able
			// to reach maximum acceleration before maximum velocity or we won't support it.
			throw new IllegalArgumentException(
				"Motors must be able to reach maximum acceleration before maximum velocity to be supported here");
		}
		val atMaxUpAcc = toMaxUpAcc.nextMoment(listBuilder, 0);
		Assert.isTrue(atMaxUpAcc.acceleration == this.maxAccel && atMaxUpAcc.velocity > 0,
			"Acceleration must reach maximum");

		val tJerkDownOne = atMaxUpAcc.acceleration / this.maxJerk
		val vJerkDownOne = maxSpeed - (tJerkDownOne * atMaxUpAcc.acceleration) +
			(this.maxJerk * tJerkDownOne * tJerkDownOne / 2.0)
		val tMaxUpAcc = (vJerkDownOne - atMaxUpAcc.velocity) / atMaxUpAcc.acceleration

		val toJerkDownOne = new ConstantAccelerationPathLeg(atMaxUpAcc, tMaxUpAcc)
		val atJerkDownOne = toJerkDownOne.nextMoment(listBuilder, -1 * this.maxJerk)

		val toConstV = new ConstantJerkPathLeg(atJerkDownOne, tJerkDownOne)
		val atConstV = toConstV.nextMoment(listBuilder, 0)

		val toJerkDownTwo = new ConstantVelocityPathLeg(atConstV, 0)
		val atJerkDownTwo = toJerkDownTwo.nextMoment(listBuilder, -1 * this.maxJerk)

		val toMaxDownAcc = new ConstantJerkPathLeg(atJerkDownTwo, this.tMaxA)
		val atMaxDownAcc = toMaxDownAcc.nextMoment(listBuilder, 0)

		val tJerkUpTwo = (0 - this.maxAccelBrake - atMaxDownAcc.acceleration) / this.maxJerk
		val vJerkUpTwo = this.maxSpeedBrk - (tJerkUpTwo * atMaxDownAcc.acceleration) -
			(this.maxJerk * tJerkUpTwo * tJerkUpTwo / 2.0)

		// Unlike the global maxAccel value, which stores an unsigned magnitude, the value we get from a PathMoment is a signed quantity!
		val tMaxDownAcc = (vJerkUpTwo - atMaxDownAcc.velocity) / atMaxDownAcc.acceleration

		val toJerkUpTwo = new ConstantAccelerationPathLeg(atMaxDownAcc, tMaxDownAcc);
		val atJerkUpTwo = toJerkUpTwo.nextMoment(listBuilder, this.maxJerk)

		val toBrakes = new ConstantJerkPathLeg(atJerkUpTwo, tJerkUpTwo)
		val atBrakes = toBrakes.nextMoment(listBuilder, 0)

		return JourneyArc.fromList(
			new ConstantAccelerationPathLeg(atBrakes, this.tStopBrk).endPath(listBuilder)
		)
	}

	private def JourneyArc computeDownwardArch(PathMoment moment, double _minSpeed) {
		var ImmutableList.Builder<IPathLeg> listBuilder = ImmutableList.<IPathLeg>builder()
		val double minSpeed = if (_minSpeed < 0) { _minSpeed } else { -1 * _minSpeed }

		var double tJerkDownOne = (this.minAccel - moment.acceleration) / this.minJerk;
		var IPathLeg toMaxDownAcc = new ConstantJerkPathLeg( moment.copy[jerk(this.minJerk)], tJerkDownOne)
		var PathMoment atMaxDownAcc = toMaxDownAcc.nextMoment(listBuilder, 0);
		
	
	    // Determine if this is a curve that will go from positive jerk directly to negative jerk, or whether it will rise, plateau, and then revert to
	    // negative jerk.  If the former is the case, then inverting the jerk until we return to zero acceleration will yield a velocity that exceeds our
	    // maximum.
		val double vAtZeroAcc = atMaxDownAcc.velocity + (this.minAccel * this.tMaxA) + (this.maxJerk * this.tMaxA * this.tMaxA / 2.0)

		// The next block picks one of two arc shapes.  Both arcs terminate at a PathMoment named "atJerkUpTwo", so the braking region that both variants
		// share can be appended with common code.
		var PathMoment atJerkUpTwo;
		if (vAtZeroAcc < minSpeed) {
			// Proceed by calculating the time reach an equilibrium point of constant velocity at our maximum velocity.  In this case all but one of the 
			// exponential terms involing jerk cancel each other out, and we are left with tmid = Math.sqrt(v(max) / j).  In thie case, t(mid) gives us
			// the duration of both the negative and positive jerk regions that we need to be left with the elevator travelling at constant velocity equal
			// in magnitude to its maximum speed value.
			//
			// The next jerk is set here to zero, not maxJerk, because the constraints of the required zero-length constant acceleration region require
			// it.  We set maxJerk at the end of that region, which works fine since it is and always will be zero-duration in length.
			listBuilder = ImmutableList.<IPathLeg>builder()
			tJerkDownOne = Math.sqrt(minSpeed / this.minJerk)
			toMaxDownAcc = new ConstantJerkPathLeg(moment.copy[jerk(this.minJerk)], tJerkDownOne)
			atMaxDownAcc = toMaxDownAcc.nextMoment(listBuilder, 0);

			// Inject a blank placeholder for first unreached constant acceleration region
			val IPathLeg toJerkUpOne = new ConstantAccelerationPathLeg(atMaxDownAcc, 0)
			val PathMoment atJerkUpOne = toJerkUpOne.nextMoment(listBuilder, this.maxJerk)

			// Reuse the duration of the first jerk down for a jerk up to cosntant velocity.
			val IPathLeg toConstV = new ConstantJerkPathLeg(atJerkUpOne, tJerkDownOne)
			val PathMoment atConstV = toConstV.nextMoment(listBuilder, 0)

            // Run at constant velocity for an instant.  This segment is extensible to run longer arcs at the same velocity.
			val IPathLeg toJerkUpTwo = new ConstantVelocityPathLeg(atConstV, 0)
			atJerkUpTwo = toJerkUpTwo.nextMoment(listBuilder, this.maxJerk)

			// Reverse the paired jerk maneuvers to brake down towards braking speed.
//	   		val IPathLeg toMaxUpAcc = new ConstantJerkPathLeg(atJerkUpTwo, tJerkDownOne)
//			atMaxUpAcc = toMaxUpAcc.nextMoment(listBuilder, 0)
		} else {
		// if (toMaxDownAcc.finalVelocity < minSpeed) {
			// 0 = (v0-vf) + a0t + 0.5 * j*t^2
			// If a(0) == 0 m/s/s, then t = sqrt(2*v(f)/j) is technically calculatable, but let's defer supporting this result and just insist that the motor must
			// be able to reach maximum acceleration before maximum velocity or we won't support it.
			// throw new IllegalArgumentException(
				// "Motors must be able to reach maximum acceleration before reaching maximum velocity to be supported here");
		// }
		// Assert.isTrue((atMaxDownAcc.acceleration == this.minAccel) && (atMaxDownAcc.velocity < 0) && (atMaxDownAcc.velocity > minSpeed),
		// 	"Acceleration must maximize magnitude of acceleration before speed going down");

			val double tJerkUpOne = (0 - atMaxDownAcc.acceleration) / this.maxJerk
			val double vJerkUpOne = minSpeed - (tJerkUpOne * atMaxDownAcc.acceleration) -
				(this.maxJerk * tJerkUpOne * tJerkUpOne / 2.0)
			val tMaxDownAcc = (vJerkUpOne - atMaxDownAcc.velocity) / atMaxDownAcc.acceleration
	
			val IPathLeg toJerkUpOne = new ConstantAccelerationPathLeg(atMaxDownAcc, tMaxDownAcc)
			val PathMoment atJerkUpOne = toJerkUpOne.nextMoment(listBuilder, this.maxJerk)
	
			val IPathLeg toConstV = new ConstantJerkPathLeg(atJerkUpOne, tJerkUpOne)
			val PathMoment atConstV = toConstV.nextMoment(listBuilder, 0)
	
			val IPathLeg toJerkUpTwo = new ConstantVelocityPathLeg(atConstV, 0)
			atJerkUpTwo = toJerkUpTwo.nextMoment(listBuilder, this.maxJerk)
	
//			val IPathLeg toMaxUpAcc = new ConstantJerkPathLeg(atJerkUpTwo, tJerkUpOne) // this.tMaxA)
//			atMaxUpAcc = toMaxUpAcc.nextMoment(listBuilder, 0)
		}

		// Working backwards from
		// a(sb) = a(0) + j*t(toBrk)
		// j*t = a(sb) - a(0)
		// v(sb) = v(0) + a(0)*t + (j*t^2)/2.0
		// v(sb) - v(0) = t*a(0) + (t*a(sb) - t*a(0))/2.0
		// t = ((v(sb) - v(0)) / (2*a(0) + a(sb) - a(0))/2.0
		// t = ((v(sb) - v(0)) * 2.0) / ( a(0) + a(sb) )
		// ... yields ...
		// t(toBrk) = 2(v(brk) - v(0)) / (a(0) + a(brk))
		// val double tJerkDownTwo = (this.minSpeedBrk - atJerkUpTwo.velocity) * 2 / (atJerkUpTwo.acceleration + this.minAccelBrake + this.minAccelBrake)
		val double tJerkUpTwo = (this.minSpeedBrk - atJerkUpTwo.velocity) * 2 / (atJerkUpTwo.acceleration + this.minAccelBrake)
		val double jerkUpTwo  = (this.minAccelBrake - atJerkUpTwo.acceleration) / tJerkUpTwo

		// Unlike the global maxAccel value, which stores an unsigned magnitude, the value we get from a PathMoment is a signed quantity!
		// val tMaxUpAcc = (vJerkDownTwo - atMaxUpAcc.velocity) / atMaxUpAcc.acceleration

//		val toJerkDownTwo = new ConstantAccelerationPathLeg(atMaxUpAcc, tMaxUpAcc);
//		val atJerkDownTwo = toJerkDownTwo.nextMoment(listBuilder, this.minJerk)

		// Placeholders for two errand legs, removed for correctness!
		val IPathLeg toOldMaxUpAcc = new ConstantJerkPathLeg(atJerkUpTwo, 0)
		val PathMoment atOldMaxUpAcc = toOldMaxUpAcc.nextMoment(listBuilder, 0)

		// Placeholders for two errand legs, removed for correctness!
		val IPathLeg toOldJerkUpOne = new ConstantAccelerationPathLeg(atOldMaxUpAcc, 0)
		val PathMoment atOldJerkUpOne = toOldJerkUpOne.nextMoment(listBuilder, jerkUpTwo)
	
		val toBrakes = new ConstantJerkPathLeg(atJerkUpTwo, tJerkUpTwo)
		val atBrakes = toBrakes.nextMoment(listBuilder, 0)

		return JourneyArc.fromList(
			new ConstantAccelerationPathLeg(atBrakes, this.tStopBrk).endPath(listBuilder)
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
}
