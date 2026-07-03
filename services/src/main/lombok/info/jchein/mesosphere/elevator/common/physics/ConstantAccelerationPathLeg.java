package info.jchein.mesosphere.elevator.common.physics;

import com.google.common.base.Preconditions;
import lombok.Getter;


@Getter
public class ConstantAccelerationPathLeg extends AbstractPathLeg {

    private final double finalVelocity;
    private final double finalHeight;
    private final double distance;

    @Override
    public DisplacementFormulaType getDisplacementFormulaType() {
        return DisplacementFormulaType.CONSTANT_ACCELERATION;
    }

    public ConstantAccelerationPathLeg(PathMoment moment, double duration) {
        super(moment, duration);
        Preconditions.checkArgument(moment.getJerk() == 0, "Constant acceleration requires zero jerk");

        var degreeTwo = duration * duration / 2.0;
        var accelerationTwo = getInitialAcceleration() * degreeTwo;

        this.finalVelocity = getInitialVelocity() + (getInitialAcceleration() * duration);
        this.finalHeight = getInitialHeight() + (getInitialVelocity() * duration) + accelerationTwo;
        this.distance = this.finalHeight - getInitialHeight();
    }

    @Override
    public double getFinalAcceleration() {
        return getInitialAcceleration();
    }

    private static class ConstantAccelerationMomentIterator extends AbstractPathLegMomentIterator {
        private double velocityDueToAcceleration;
        private double heightDueToAcceleration;

        ConstantAccelerationMomentIterator(
                PathMoment initialMoment, double firstTickDuration, double tickDuration, double finalTickTime) {
            super(initialMoment, firstTickDuration, tickDuration, finalTickTime);
        }

        @Override
        public void setTickDuration(double tickDuration) {
            this.velocityDueToAcceleration = getCurrentAcceleration() * tickDuration;
            this.heightDueToAcceleration = this.velocityDueToAcceleration * tickDuration / 2.0;
        }

        @Override
        public double getNextAcceleration() {
            return getCurrentAcceleration();
        }

        @Override
        public double getNextVelocity() {
            return getCurrentVelocity() + this.velocityDueToAcceleration;
        }

        @Override
        public double getNextHeight() {
            return getCurrentHeight() + (getCurrentVelocity() * this.tickDuration) + this.heightDueToAcceleration;
        }
    }

    @Override
    ConstantAccelerationMomentIterator getMomentIterator(double firstTickDuration, double tickDuration) {
        return new ConstantAccelerationMomentIterator(
            getInitialMoment(), firstTickDuration, tickDuration, getFinalTime());
    }

    @Override
    ConstantAccelerationPathLeg doTruncate(double newInitialTime) {
        Preconditions.checkArgument(getInitialTime() <= newInitialTime);
        Preconditions.checkArgument(newInitialTime <= getFinalTime());

        return new ConstantAccelerationPathLeg(
            new ConstantAccelerationMomentIterator(
                getInitialMoment(),
                newInitialTime - getInitialMoment().getTime(),
                getFinalTime() - newInitialTime,
                getFinalTime()).next(),
            getFinalTime() - newInitialTime
        );
    }
}
