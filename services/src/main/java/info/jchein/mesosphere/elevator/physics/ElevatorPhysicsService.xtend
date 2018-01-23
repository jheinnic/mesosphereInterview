package info.jchein.mesosphere.elevator.physics

import com.google.common.base.Preconditions
import org.eclipse.xtend.lib.annotations.ToString

import static extension java.lang.Math.floor
import static extension java.lang.Math.round
import static extension java.lang.String.format

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
	double vToCnstVUp
	double dToCnstVUp
	double tUpAtMaxA
	double dUpAtMaxA
	double dFromCnstVUp
	double vFromCnstVUp
	double accelBrake
	double tFromCnstVUp
	double dMinForConstVUp
	double tMinForConstVUp

	val int numFloors
	val double metersPerFloor
	val double maxJerk
	val double maxAccel
	var double speedMax
	val double speedBrk
	val double distBrk

	double[] travelUpByDistance
	double[] timeUpByDistance

	double[] travelDownByDistance
	double[] timeDownByDistance

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
		this.accelBrake = speedBrk * speedBrk / (2 * this.distBrk)
		this.tStopBrk = speedBrk / this.accelBrake

		Preconditions.checkArgument(
			this.accelBrake < maxAccel,
			"It must be possible to brake from the stopping distance at constant rate of velocity change"
		);

		this.travelUpByDistance = newDoubleArrayOfSize(this.numFloors - 1);
		this.timeUpByDistance = newDoubleArrayOfSize(this.numFloors - 1);
		this.travelDownByDistance = newDoubleArrayOfSize(this.numFloors - 1);
		this.timeDownByDistance = newDoubleArrayOfSize(this.numFloors - 1);

		this.prefillSpaceAndTimeArrays(this.motorProps.maxRiseSpeed, this.travelUpByDistance, this.timeUpByDistance)
		this.prefillSpaceAndTimeArrays(this.motorProps.maxDescentSpeed, this.travelDownByDistance,
			this.timeDownByDistance)
		this.prefillSpaceAndTimeArrays(this.motorProps.maxRiseSpeed, this.travelUpByDistance, this.timeUpByDistance)
		this.prefillSpaceAndTimeArrays(this.motorProps.maxDescentSpeed, this.travelDownByDistance,
			this.timeDownByDistance)
	}

	def void prefillSpaceAndTimeArrays(double maxSpeed, double[] spaceArray, double[] timeArray) {
//		var printUp = ""
//		var deltaUp = 0.0
//		var lastUp = 0.0

		val floorHeight = this.metersPerFloor
		var ii = this.numFloors - 1;

		this.speedMax = maxSpeed
		this.preComputeConstantRegions();

		for (var nextHeight = (floorHeight * ii), ii = ii - 1; ii >= 0; ii--, nextHeight -= floorHeight) {
			var constVelocitySegmentLength = nextHeight - this.dMinForConstVUp
			if (constVelocitySegmentLength < 0) {
//				printUp +=
//					"Discarding %d: (%f -> %f)\n".format(ii, constVelocitySegmentLength,
//						(this.tMinForConstVUp + (constVelocitySegmentLength / this.speedMax)));
				this.speedMax = computeShortPathMaxSpeed(nextHeight);
//				printUp += "New speedMax = %f\n".format(this.speedMax);
				this.preComputeConstantRegions();
				constVelocitySegmentLength = nextHeight - this.dMinForConstVUp
			}
			spaceArray.set(ii, constVelocitySegmentLength)
			timeArray.set(ii, this.tMinForConstVUp + (constVelocitySegmentLength / this.speedMax))
		}

//		for (ii = 0; ii < this.numFloors - 1; ii++) {
//			deltaUp = timeArray.get(ii) - lastUp;
//			lastUp = timeArray.get(ii);
//
//			printUp += "%d: (%f -> %f) @ %f\n".format(ii, spaceArray.get(ii), timeArray.get(ii), deltaUp)
//		}
//		println("XX: %f in %f\n".format(this.dMinForConstVUp, this.tMinForConstVUp));
//		println("\nData:")
//		println(printUp);
//		println("%f".format(timeArray.get(3) + timeArray.get(2) + timeArray.get(4)))
//		println("%f".format(timeArray.get(9)))
//		println(this.toString())
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
	 */
	private def preComputeConstantRegions() {
		// The time to reduce acceleration from max to 0 is the same as needed to increase it from 0 to max.  Account
		// for an interval required at negative constant jerk to switch from constant acceleration to constant and maximal
		// velocity.  
		//
		// Compute the velocity when this reduction begins in order to achieve and sustain stated maximum speeds without
		// exceeding maximum jerk when we eventually have to reduce the acceleration towards 0.  For floors with enough
		// distance between them to support reaching maximum speed, the remaining distance following the pre-computed
		// intervals from this block and the two that follow will be accounted for in constant velocity at maximum speed.
		this.vToCnstVUp = this.speedMax - (this.tMaxA * this.maxAccel) + this.vMaxA
		this.dToCnstVUp = (this.vToCnstVUp * this.tMaxA) + (this.tMaxA * this.tMaxA * this.maxAccel / 2.0) - this.dMaxA

		// Now that we know the velocity where constant acceleration begins tapering off, compute the time it takes to
		// reach that speed at constant acceleration, and the distance traveled in that time.  This interval is computed
		// after the transition from constant acceleration to constant velocity, but it occurs before that interval.  We
		// need to proceed this way because we need to know the velocity moment that ends this interval before we can
		// compute its duration in time and space.
		this.tUpAtMaxA = (this.vToCnstVUp - this.vMaxA) / this.maxAccel
		this.dUpAtMaxA = (this.vMaxA * this.tUpAtMaxA) + (this.tUpAtMaxA * this.tUpAtMaxA * this.maxAccel / 2.0)

		// The remaining interval accounts for a transition from constant maximum velocity to slow down to braking speed
		// before we reach the final braking distance zone.  This computation also serves to inform the greatest floor 
		// distance from which we can safely accept a destination change to an earlier floor along the same trajectory.
		//
		// Scratch that.  Target breaking acceleration and velocity.  Compensate for any difference when calculating the
		// straightforward constant velocity duration on each floor pair.
		// this.tFromCnstVUp = (speedMax - this.speedBrk) / this.accelBrake
		this.tFromCnstVUp = (this.maxAccel - this.accelBrake) / this.maxJerk
		this.vFromCnstVUp = this.speedBrk
		this.dFromCnstVUp = (this.speedMax * this.tFromCnstVUp) -
			(this.maxAccel * this.tFromCnstVUp * this.tFromCnstVUp / 2.0) +
			(this.maxJerk * this.tFromCnstVUp * this.tFromCnstVUp * this.tFromCnstVUp / 6.0)

		// Last step--sum up the distance and time costs for the regions calculated thus far.  For any traversal path 
		// that has a region of time spent at constant velocity, the course of action is to deduct the distance sum from
		// the total distance, then divide the difference by maximum speed to compute the constant velocity region's time
		// cost.  Add that to the time measurement for the variable regions that are the same for all paths that reach
		// maximum velocity.
		this.tMinForConstVUp = this.tMaxA + this.tMaxA + this.tUpAtMaxA + this.tFromCnstVUp + this.tStopBrk;
		this.dMinForConstVUp = this.dMaxA + this.dToCnstVUp + this.dUpAtMaxA + this.dFromCnstVUp + this.distBrk

	}

	override expectedStopDuration(int boardingCount, int disembarkingCount) {
		return this.doorProps.doorOpenCloseSlideTime + Math.max(
			this.doorProps.minDoorHoldTimePerOpen,
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
		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

	override travelTime(int fromFloorIndex, int toFloorIndex) {
		if (fromFloorIndex > toFloorIndex) {
			return this.travelDownByDistance.get(fromFloorIndex - toFloorIndex);
		} else if (fromFloorIndex < toFloorIndex) {
			return this.travelUpByDistance.get(toFloorIndex - fromFloorIndex);
		} else {
			throw new IllegalArgumentException(
				"to and from floor indices cannot both be <%d> and <%d>".format(fromFloorIndex, toFloorIndex));
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
