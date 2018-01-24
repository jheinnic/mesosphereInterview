package info.jchein.mesosphere.elevator.physics

import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableList
import org.eclipse.xtend.lib.annotations.ToString
import org.springframework.util.Assert

import info.jchein.mesosphere.elevator.configuration.properties.BuildingProperties
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorMotorProperties
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorDoorProperties
import info.jchein.mesosphere.elevator.configuration.properties.PassengerToleranceProperties
import info.jchein.mesosphere.elevator.configuration.properties.ElevatorWeightProperties

import static extension java.lang.String.format
import static extension java.lang.Math.floor
import static extension java.lang.Math.round
//import static extension java.lang.Math.sqrt

@ToString
class ElevatorPhysicsService implements IElevatorPhysicsService {
	val BuildingProperties bldgProps;
	val ElevatorDoorProperties doorProps;
	val ElevatorMotorProperties motorProps;
	val ElevatorWeightProperties weightProps;
	val PassengerToleranceProperties toleranceProps;

	final double tMaxA
	final double vMaxA
	final double dMaxA

	double tStopBrk
	double accelBrake

	val int numFloors
	val double metersPerFloor
	val double maxJerk
	val double maxAccel
	val double speedBrk
	val double distBrk

	JourneyArc[] upwardArcs
	JourneyArc[] downwardArcs

	public new(
		BuildingProperties bldgProps,
		ElevatorDoorProperties doorProps,
		ElevatorMotorProperties motorProps,
		ElevatorWeightProperties weightProps,
		PassengerToleranceProperties toleranceProps
	) {
		this.bldgProps = bldgProps;
		this.doorProps = doorProps;
		this.motorProps = motorProps;
		this.weightProps = weightProps;
		this.toleranceProps = toleranceProps;

		this.numFloors = this.bldgProps.numFloors;
		this.metersPerFloor = this.bldgProps.metersPerFloor;
		this.maxJerk = this.motorProps.maxJerk
		this.maxAccel = this.motorProps.maxAcceleration
		this.speedBrk = this.motorProps.brakingSpeed
		this.distBrk = this.motorProps.brakingDistance

		// Compute the time required to reach maximum acceleration, given maximum jerk, the resulting velocity, and the
		// distance traveled in that time.
		this.tMaxA = maxAccel / maxJerk;
		this.vMaxA = maxJerk * tMaxA * tMaxA / 2.0;
		this.dMaxA = vMaxA * tMaxA / 3.0;

		// Compute the deceleration required to stop from braking speed in the given stopping distance at constant
		// deceleration.  Derive the time to complete a stop from the braking distance marker using that acceleration
		// rate and confirm that it requires less than the maximum acceleration magnitude to stop from slow speed at the
		// given distance.
		//
		// s = s0 + v0*t + (0.5)*a*t^2
		// v = v0 + a*t
		// v^2 = v0^2 + 2a(s - s0)
		this.accelBrake = speedBrk * speedBrk / (2 * this.distBrk)
		this.tStopBrk = speedBrk / this.accelBrake

		Preconditions.checkArgument(
			this.accelBrake < maxAccel,
			"It must be possible to brake from the stopping distance at constant rate of velocity change"
		);

		this.upwardArcs = newArrayOfSize(this.numFloors - 1);
		this.downwardArcs = newArrayOfSize(this.numFloors - 1);

		val floorHeight = this.metersPerFloor
		var lastFloorPair = this.numFloors - 1;

		val slowRise = this.computeUpwardArch(PathMoment.build [
			it.time(0).height(0).velocity(0).acceleration(0).jerk(0)
		], this.motorProps.slowSpeed)
		val slowDescent = this.computeDownwardArch(PathMoment.build [
			it.time(0).height(0).velocity(0).acceleration(0).jerk(0)
		], -1 * this.motorProps.slowSpeed)
		val fastRise = this.computeUpwardArch(PathMoment.build [
			it.time(0).height(0).velocity(0).acceleration(0).jerk(0)
		], this.motorProps.maxRiseSpeed)
		val fastDescent = this.computeDownwardArch(PathMoment.build [
			it.time(0).height(0).velocity(0).acceleration(0).jerk(0)
		], this.motorProps.maxDescentSpeed)

		fastRise.legIterator().forEach[ nextLeg | println(nextLeg.toString()); ]

		Assert.isTrue(slowDescent.shortestPossibleArc <= floorHeight,
			"Must be able to traverse one floor within slow speed arc's path")
			
		var ii = 0
		var nextHeight = floorHeight
		var fastArcDistance = fastRise.shortestPossibleArc

		for (; ii < lastFloorPair && nextHeight < fastArcDistance; ii++) {
			System.out.println(String.format("%d is slow", ii));
			this.upwardArcs.set(
				ii, slowRise.adjustConstantRegion(nextHeight)
			)	
			this.downwardArcs.set(
				ii, slowDescent.adjustConstantRegion(nextHeight)
			)	
			nextHeight += floorHeight
		}
		for (var jj=ii; jj<lastFloorPair; jj++) {
			System.out.println(String.format("%d is fast", jj));
			this.upwardArcs.set(
				jj, fastRise.adjustConstantRegion(nextHeight)
			)	
			this.downwardArcs.set(
				jj, fastDescent.adjustConstantRegion(nextHeight)
			)	
			nextHeight += floorHeight
		}
	}

	/**
	 * For paths that are sufficiently short, there is no span at constant velocity.  Between the starting point and the
	 * half-way point there is one region of constant jerk with increasing acceleration, followed by a second span where
	 * the body is at constant acceleration, but does not achieve constant speed before reaching its apex.  However, there
	 * is symmetry between both sides of the curve, and we can therefore use that halfway point to find a usable maximum velocity
	 * and recompute.
	 */
	private def double computeShortPathMaxSpeed(double maxDistance) {
		val d0 = this.dMaxA
		val dMid = (maxDistance - d0) / 2; // - this.distBrk) / 2
		val v0 = this.vMaxA
		return Math.sqrt((v0 * v0) + (2 * this.maxAccel * dMid));
	}

	private def PathMoment nextMoment(PathLeg pathLeg, ImmutableList.Builder<PathLeg> listBuilder, double nextJerk) {
		listBuilder.add(pathLeg);
		return pathLeg.getFinalMoment(nextJerk)
	}

	private def ImmutableList<PathLeg> endPath(PathLeg finalLeg, ImmutableList.Builder<PathLeg> listBuilder) {
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
 		val listBuilder = ImmutableList.<PathLeg>builder()
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
			throw new IllegalArgumentException("Motors must be able to reach maximum acceleration before maximum velocity to be supported here");
		}
		val atMaxUpAcc = toMaxUpAcc.nextMoment(listBuilder, 0);
		Assert.isTrue(atMaxUpAcc.acceleration == this.maxAccel && atMaxUpAcc.velocity > 0, "Acceleration must reach maximum");

        val tJerkDownOne = atMaxUpAcc.acceleration / this.maxJerk
		val vJerkDownOne = maxSpeed - (tJerkDownOne * atMaxUpAcc.acceleration) + (this.maxJerk * tJerkDownOne * tJerkDownOne / 2.0)
		val tMaxUpAcc = (vJerkDownOne - atMaxUpAcc.velocity) / atMaxUpAcc.acceleration

		val toJerkDownOne = new ConstantAccelerationPathLeg(atMaxUpAcc, tMaxUpAcc)
		val atJerkDownOne = toJerkDownOne.nextMoment(listBuilder, -1 * this.maxJerk)

		val toConstV = new ConstantJerkPathLeg(atJerkDownOne, tJerkDownOne)
		val atConstV = toConstV.nextMoment(listBuilder, 0)

		val toJerkDownTwo = new ConstantVelocityPathLeg(atConstV, 0)
		val atJerkDownTwo = toJerkDownTwo.nextMoment(listBuilder, -1 * this.maxJerk)

		val toMaxDownAcc = new ConstantJerkPathLeg(atJerkDownTwo, this.tMaxA)
		val atMaxDownAcc = toMaxDownAcc.nextMoment(listBuilder, 0)

		val tJerkUpTwo = (0 - this.accelBrake - atMaxDownAcc.acceleration) / this.maxJerk
		val vJerkUpTwo = this.speedBrk - (tJerkUpTwo * atMaxDownAcc.acceleration) - (this.maxJerk * tJerkUpTwo * tJerkUpTwo / 2.0)
		
		// Unlike the global maxAccel value, which stores an unsigned magnitude, the value we get from a PathMoment is a signed quantity!
		val tMaxDownAcc = -1 * (atMaxDownAcc.velocity - vJerkUpTwo) / atMaxDownAcc.acceleration

		val toJerkUpTwo = new ConstantAccelerationPathLeg(atMaxDownAcc, tMaxDownAcc);
		val atJerkUpTwo = toJerkUpTwo.nextMoment(listBuilder, this.maxJerk)

		val toBrakes = new ConstantJerkPathLeg(atJerkUpTwo, tJerkUpTwo)
		val atBrakes = toBrakes.nextMoment(listBuilder, 0)

		return JourneyArc.fromList(
			new ConstantAccelerationPathLeg(atBrakes, this.tStopBrk).endPath(listBuilder)
		)
	}

	private def JourneyArc computeDownwardArch(PathMoment moment, double _maxSpeed) {
		val listBuilder = ImmutableList.<PathLeg>builder()
		val maxSpeed = if (_maxSpeed < 0) {
				_maxSpeed
			} else {
				-1 * _maxSpeed
			}

		val tJerkDownOne = (this.maxAccel + moment.acceleration) / this.maxJerk;
		val toMaxDownAcc = new ConstantJerkPathLeg( moment.copy[jerk(-1 * this.maxJerk)], tJerkDownOne)
		if (toMaxDownAcc.finalVelocity < maxSpeed) {
			// 0 = (v0-v) + a0t + 0.5 * j*t^2 is technically a factor-able polynomial, but let's defer supporting this result and just insist that the motor must be able
			// to reach maximum acceleration before maximum velocity or we won't support it.
			throw new IllegalArgumentException("Motors must be able to reach maximum acceleration before maximum velocity to be supported here");
		}
		val atMaxDownAcc = toMaxDownAcc.nextMoment(listBuilder, 0);
		Assert.isTrue(atMaxDownAcc.acceleration == (-1 * this.maxAccel) && atMaxDownAcc.velocity < 0, "Acceleration must reach maximum");

        val tJerkUpOne = -1 * atMaxDownAcc.acceleration / this.maxJerk
		val vJerkUpOne = maxSpeed - (tJerkUpOne * atMaxDownAcc.acceleration) - (this.maxJerk * tJerkUpOne * tJerkUpOne / 2.0)
		val tMaxDownAcc = (vJerkUpOne - atMaxDownAcc.velocity) / atMaxDownAcc.acceleration

		val toJerkUpOne = new ConstantAccelerationPathLeg(atMaxDownAcc, tMaxDownAcc)
		val atJerkUpOne = toJerkUpOne.nextMoment(listBuilder, this.maxJerk)

		val toConstV = new ConstantJerkPathLeg(atJerkUpOne, tJerkUpOne)
		val atConstV = toConstV.nextMoment(listBuilder, 0)

		val toJerkUpTwo = new ConstantVelocityPathLeg(atConstV, 0)
		val atJerkUpTwo = toJerkUpTwo.nextMoment(listBuilder, this.maxJerk)

		val toMaxUpAcc = new ConstantJerkPathLeg(atJerkUpTwo, this.tMaxA)
		val atMaxUpAcc = toMaxUpAcc.nextMoment(listBuilder, 0)

		// TODO: Proofcheck the signage here!
		val tJerkDownTwo = (atMaxUpAcc.acceleration - this.accelBrake) / this.maxJerk
		val vJerkDownTwo = (-1 * this.speedBrk) - (tJerkDownTwo * atMaxUpAcc.acceleration) + ((this.maxJerk * tJerkDownTwo * tJerkDownTwo) / 2.0)

		// Unlike the global maxAccel value, which stores an unsigned magnitude, the value we get from a PathMoment is a signed quantity!
		val tMaxUpAcc = (vJerkDownTwo - atMaxUpAcc.velocity) / atMaxUpAcc.acceleration

		val toJerkDownTwo = new ConstantAccelerationPathLeg(atMaxUpAcc, tMaxUpAcc);
		val atJerkDownTwo = toJerkDownTwo.nextMoment(listBuilder, -1 * this.maxJerk)
		
		
//		val toMaxDownAcc = new ConstantJerkPathLeg(atJerkDownTwo, this.tMaxA)
//		val atMaxDownAcc = toMaxDownAcc.nextMoment(listBuilder, 0)
//
//		val tJerkUpTwo = (0 - this.accelBrake - atMaxDownAcc.acceleration) / this.maxJerk
//		val vJerkUpTwo = this.speedBrk - (tJerkUpTwo * atMaxDownAcc.acceleration) - (this.maxJerk * tJerkUpTwo * tJerkUpTwo / 2.0)
//		
//		// Unlike the global maxAccel value, which stores an unsigned magnitude, the value we get from a PathMoment is a signed quantity!
//		val tMaxDownAcc = -1 * (atMaxDownAcc.velocity - vJerkUpTwo) / atMaxDownAcc.acceleration

		
		
		

		val toBrakes = new ConstantJerkPathLeg(atJerkDownTwo, tJerkDownTwo)
		val atBrakes = toBrakes.nextMoment(listBuilder, 0)

		return JourneyArc.fromList(
			new ConstantAccelerationPathLeg(atBrakes, this.tStopBrk).endPath(listBuilder)
		)
	}

	override expectedStopDuration(int boardingCount, int disembarkingCount) {
		return this.doorProps.doorOpenCloseSlideTime + Math.max(this.doorProps.minDoorHoldTimePerOpen,
			(this.doorProps.doorHoldTimePerPerson * (boardingCount + disembarkingCount)));
	}

	override idealPassengerCount() {
		return (this.weightProps.idealWeightLoad / this.weightProps.passengerWeight).round as int
	}

	override maxTolerancePassengerCount() {
		return ((
			this.weightProps.maxWeightLoad * this.toleranceProps.getRefusePickupAfterWeightPct
		) / this.weightProps.passengerWeight).floor as int
	}

	override floorDistance(int fromFloorIndex, int toFloorIndex) {
		return (toFloorIndex - fromFloorIndex) * this.bldgProps.metersPerFloor;
	}

	override double travelTime(int fromFloorIndex, int toFloorIndex) {
		if (fromFloorIndex > toFloorIndex) {
			return this.downwardArcs.get(fromFloorIndex - toFloorIndex).duration;
		} else if (fromFloorIndex < toFloorIndex) {
			return this.upwardArcs.get(toFloorIndex - fromFloorIndex).duration;
		} else {
			throw new IllegalArgumentException(
				"to and from floor indices cannot both be <%d> and <%d>".format(fromFloorIndex, toFloorIndex));
		}
	}

	def double travelDuration(ImmutableList<PathLeg> path) {
		return path.get(path.size() - 1).finalTime - path.get(0).initialTime
	}

	def double travelDistance(ImmutableList<PathLeg> path) {
		val displacement = path.get(path.size() - 1).finalHeight - path.get(0).initialHeight

		return if (displacement < 0) {
			displacement * -1
		} else {
			displacement
		}
	}
	
	override getTraversalPath(int fromFloorIndex, int toFloorIndex) {
		if (fromFloorIndex < toFloorIndex) {
			return this.upwardArcs.get(toFloorIndex - fromFloorIndex - 1)
		} else {
			return this.downwardArcs.get(fromFloorIndex - toFloorIndex - 1)
		}
	}
}

/*o		//
 * 		// We can reuse information from the reduction of acceleration from maximum to 0 as we now proceed from 0 to
 * 		// negative maximum while continuing to respect maximum jerk magnitude.  This will require the same amount of time,
 * 		// so we can reuse the time to max acceleration at maximal jerk for time again.  We would also reuse the previously
 * 		// computed transition velocity, but now it is the target velocity, not the origin, and so is not in the formula.
 * 		//
 * 		// The most significant difference is that the 'starting acceleration' term from the distance formula drops out
 * 		// here, because we are starting at 0 acceleration.  The sign of the jerk term remains negative.
 * 		this.vFromCnstVUp = this.vToCnstVUp
 * 		this.dFromCnstVUp = (speedUp * this.tMaxA) - this.dMaxA
 * 		this.vFromCnstVDown = this.vToCnstVDown
 * 		this.dFromCnstVDown = (speedDown * this.tMaxA) - this.dMaxA
 * 		
 * 		// An interesting result is that while dFromCnstVUp and dToConstVUp represent very different magnitudes of change
 * 		// from what their original velocity would yield in the same time, they yield the same absolute value for distance
 * 		// traveled.  This suggests a means to compute these intervals when the distance traveled is too short to reach
 * 		// maximum speed.
 * 		//
 * 		// The final interval will not reflect the same symmetry, as it targets braking speed, not initial velocity after
 * 		// ramping to maximum acceleration from rest.
 * //		this.tUpAtMinA = (this.vFromCnstVUp - speedBrk) / this.accelBrake
 * //		this.dUpAtMinA = (this.vFromCnstVUp * this.tUpAtMinA) + (this.tUpAtMinA * this.tUpAtMinA * maxAccel / 2.0)
 * //		this.tDownAtMinA = (this.vFromCnstVDown - this.vMaxA) / maxAccel
 * //		this.dDownAtMinA = (this.vMaxA * this.tDownAtMinA) + (this.tDownAtMinA * this.tDownAtMinA * maxAccel / 2.0)

 * 		var double tDeltaSlow
 * 		var double dDeltaSlow
 * 		if (speedBrk < vMaxA) { 
 * 			tMaxSlow = (2.0 * speedBrk / maxJerk).sqrt
 * 			dMaxSlow = speedBrk * tMaxSlow / 3.0
 * 		} else {
 * 			tDeltaSlow = ((speedBrk-vMaxA) / maxAccel)
 * 			dDeltaSlow = (vMaxA * tDeltaSlow) + (maxAccel * tDeltaSlow * tDeltaSlow / 2.0)
 * 			tMaxSlow = tMaxA + tDeltaSlow
 * 			dMaxSlow = dMaxA + dDeltaSlow
 * 		}
 * 		
 * 		Preconditions.checkArgument(
 * 			dMaxSlow < dBrk, "Braking distance must be at least as great as distance required to stop at slow speed"
 * 		);

 * 		val tDeltaUp = (speedUp - vMaxA) / maxAccel
 * 		val tMaxUp = tMaxA + tDeltaUp
 * 		val dDeltaUp = (vMaxA * tDeltaUp) + (maxAccel * tDeltaUp * tDeltaUp / 2.0)
 * 		val dMaxUp = dMaxA + dDeltaUp
 * 	
 * 		val tDeltaDown = (speedDown - vMaxA) / maxAccel
 * 		val tMaxDown = tMaxA + tDeltaDown
 * 		val dDeltaDown = (vMaxA * tDeltaDown) + (maxAccel * tDeltaDown * tDeltaDown / 2.0)
 * 		val dMaxDown = dMaxA + dDeltaDown

 * 		Preconditions.checkArgument(
 * 			tMaxUp > 0 && tMaxDown > 0,
 * 			"Elevators that cannot achieve constant acceleration are unsupported"
 * 		);
 * 		var double accelerationAtMaxRise
 * 		var double accelerationAtMaxFall
 * 		var double accelerationAtBrakingSpeed
 */
