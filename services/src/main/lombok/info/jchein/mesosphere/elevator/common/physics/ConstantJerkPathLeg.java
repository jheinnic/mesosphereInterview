package info.jchein.mesosphere.elevator.common.physics;

import com.google.common.base.Preconditions;
import lombok.Getter;


@Getter
public class ConstantJerkPathLeg extends AbstractPathLeg {

    private final double finalAcceleration;
    private final double finalVelocity;
    private final double finalHeight;
    private final double distance;

    @Override
    public DisplacementFormulaType getDisplacementFormulaType() {
        return DisplacementFormulaType.NON_ZERO_JERK;
    }

    public ConstantJerkPathLeg(PathMoment moment, double duration) {
        super(moment, duration);

        var degreeTwo = duration * duration / 2.0;
        var jerk = getJerk();
        var jerkTwo = jerk * degreeTwo;
        var accelerationTwo = getInitialAcceleration() * degreeTwo;
        var jerkThree = jerkTwo * duration / 3.0;

        this.finalAcceleration = getInitialAcceleration() + (jerk * duration);
        this.finalVelocity = getInitialVelocity() + (getInitialAcceleration() * duration) + jerkTwo;
        this.finalHeight = getInitialHeight() + (getInitialVelocity() * duration) + accelerationTwo + jerkThree;
        this.distance = this.finalHeight - getInitialHeight();
    }

    /**
     * Encapsulates the math for incrementing a PathMoment through constant jerk. Extracted so the same
     * logic handles both the irregular first-tick offset and the uniform steady-state ticks, letting
     * getMomentIterator start the sequence at any arbitrary point along the leg.
     */
    private static class ConstantJerkMomentIterator extends AbstractPathLegMomentIterator {
        private double tickDegreeOne;
        private double tickDegreeTwo;
        private double accelerationDueToJerk;
        private double velocityDueToJerk;
        private double heightDueToJerk;
        private final double jerk;

        ConstantJerkMomentIterator(
                PathMoment initialMoment, double initialTickDuration, double tickDuration, double finalTime) {
            super(initialMoment, initialTickDuration, tickDuration, finalTime);
            this.jerk = initialMoment.getJerk();
        }

        @Override
        public void setTickDuration(double tickDuration) {
            this.tickDegreeOne = tickDuration;
            this.tickDegreeTwo = tickDuration * tickDuration / 2.0;
            this.accelerationDueToJerk = tickDuration * this.jerk;
            this.velocityDueToJerk = this.accelerationDueToJerk * tickDuration / 2.0;
            this.heightDueToJerk = this.velocityDueToJerk * tickDuration / 3.0;
        }

        @Override
        public double getNextAcceleration() {
            return getCurrentAcceleration() + this.accelerationDueToJerk;
        }

        @Override
        public double getNextVelocity() {
            return getCurrentVelocity() + (getCurrentAcceleration() * this.tickDegreeOne) + this.velocityDueToJerk;
        }

        @Override
        public double getNextHeight() {
            return getCurrentHeight()
                + (getCurrentVelocity() * this.tickDegreeOne)
                + (getCurrentAcceleration() * this.tickDegreeTwo)
                + this.heightDueToJerk;
        }
    }

    @Override
    ConstantJerkMomentIterator getMomentIterator(double firstTickDuration, double tickDuration) {
        return new ConstantJerkMomentIterator(
            getInitialMoment(), firstTickDuration, tickDuration, getFinalTime());
    }

    @Override
    ConstantJerkPathLeg doTruncate(double newInitialTime) {
        Preconditions.checkArgument(getInitialTime() <= newInitialTime);
        Preconditions.checkArgument(newInitialTime <= getFinalTime());

        return new ConstantJerkPathLeg(
            new ConstantJerkMomentIterator(
                getInitialMoment(),
                newInitialTime - getInitialMoment().getTime(),
                getFinalTime() - newInitialTime,
                getFinalTime()).next(),
            getFinalTime() - newInitialTime
        );
    }
}
