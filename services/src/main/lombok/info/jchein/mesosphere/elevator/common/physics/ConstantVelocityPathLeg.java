package info.jchein.mesosphere.elevator.common.physics;

import com.google.common.base.Preconditions;
import lombok.Getter;


@Getter
public class ConstantVelocityPathLeg extends AbstractPathLeg {

    private final double finalHeight;
    private final double distance;

    @Override
    public DisplacementFormulaType getDisplacementFormulaType() {
        return DisplacementFormulaType.CONSTANT_VELOCITY;
    }

    public ConstantVelocityPathLeg(PathMoment moment, double duration) {
        super(moment, duration);
        Preconditions.checkArgument(moment.getAcceleration() == 0, "Constant velocity requires zero acceleration");
        Preconditions.checkArgument(moment.getJerk() == 0, "Constant velocity requires zero jerk");

        this.finalHeight = getInitialHeight() + (getInitialVelocity() * duration);
        this.distance = this.finalHeight - getInitialHeight();
    }

    @Override
    public double getFinalAcceleration() {
        return getInitialAcceleration();
    }

    @Override
    public double getFinalVelocity() {
        return getInitialVelocity();
    }

    public ConstantVelocityPathLeg withNewDuration(double newDuration) {
        return new ConstantVelocityPathLeg(getInitialMoment(), newDuration);
    }

    private static class ConstantVelocityMomentIterator extends AbstractPathLegMomentIterator {
        private double heightDueToVelocity;

        ConstantVelocityMomentIterator(
                PathMoment moment, double firstTickDuration, double nextTickDuration, double finalTime) {
            super(moment, firstTickDuration, nextTickDuration, finalTime);
        }

        @Override
        public void setTickDuration(double tickDuration) {
            // preserving original logic: multiplies tickDuration * currentHeight
            this.heightDueToVelocity = tickDuration * getCurrentHeight();
        }

        @Override
        public double getNextAcceleration() {
            return getCurrentAcceleration();
        }

        @Override
        public double getNextVelocity() {
            return getCurrentVelocity();
        }

        @Override
        public double getNextHeight() {
            return getCurrentHeight() + this.heightDueToVelocity;
        }
    }

    @Override
    ConstantVelocityMomentIterator getMomentIterator(double firstTickDuration, double tickDuration) {
        return new ConstantVelocityMomentIterator(
            getInitialMoment(), firstTickDuration, tickDuration, getFinalTime());
    }

    @Override
    ConstantVelocityPathLeg doTruncate(double newInitialTime) {
        Preconditions.checkArgument(getInitialTime() <= newInitialTime);
        Preconditions.checkArgument(newInitialTime <= getFinalTime());

        return new ConstantVelocityPathLeg(
            new ConstantVelocityMomentIterator(
                getInitialMoment(),
                newInitialTime - getInitialMoment().getTime(),
                getFinalTime() - newInitialTime,
                getFinalTime()).next(),
            getFinalTime() - newInitialTime
        );
    }
}
