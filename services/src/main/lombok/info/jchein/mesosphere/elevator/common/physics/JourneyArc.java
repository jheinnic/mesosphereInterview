package info.jchein.mesosphere.elevator.common.physics;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import lombok.Builder;
import lombok.Value;


@Value
@Builder(toBuilder = true)
public class JourneyArc implements Iterable<IPathLeg>
{
    private static final Interner<JourneyArc> INTERN_CACHE = Interners.newWeakInterner();

    PathMoment initialMoment;
    PathMoment terminalMoment;
    ConstantJerkPathLeg forwardJerkOne;
    ConstantAccelerationPathLeg forwardAcceleration;
    ConstantJerkPathLeg reverseJerkOne;
    ConstantVelocityPathLeg constantVelocity;
    ConstantJerkPathLeg reverseJerkTwo;
    ConstantJerkPathLeg forwardJerkTwo;
    ConstantJerkPathLeg terminalSegment;


    public static JourneyArc build(Consumer<JourneyArcBuilder> director) {
        JourneyArcBuilder bldr = JourneyArc.builder();
        director.accept(bldr);
        return INTERN_CACHE.intern(bldr.build());
    }


    @Override
    public java.util.Iterator<IPathLeg> iterator() {
        return new JourneyArcPathLegIterator(this);
    }


    public JourneyArcMomentSeries asMomentIterable(double tickDuration) {
        return new JourneyArcMomentSeries(this, this.initialMoment, this.terminalMoment, tickDuration);
    }


    public DirectionOfTravel getDirection() {
        if (this.initialMoment.getHeight() < this.terminalMoment.getHeight()) return DirectionOfTravel.GOING_UP;
        if (this.initialMoment.getHeight() > this.terminalMoment.getHeight()) return DirectionOfTravel.GOING_DOWN;
        return DirectionOfTravel.STOPPED;
    }


    public double getDuration() {
        return this.terminalMoment.getTime() - this.initialMoment.getTime();
    }


    public double distance() {
        switch (getDirection()) {
            case GOING_UP:   return this.terminalMoment.getHeight() - this.initialMoment.getHeight();
            case GOING_DOWN: return this.initialMoment.getHeight() - this.terminalMoment.getHeight();
            default:         return 0;
        }
    }


    public PathMoment getBrakeAppliedMoment() {
        return this.constantVelocity.getFinalMoment(0);
    }


    public double getShortestPossibleArc() {
        switch (getDirection()) {
            case GOING_UP:   return distance() - this.constantVelocity.getDistance();
            case GOING_DOWN: return distance() + this.constantVelocity.getDistance();
            default:         return 0;
        }
    }


    /**
     * Adjusts the constant-velocity region to cover a new absolute travel distance.
     * The acceleration/deceleration profile is unchanged; only the cruise duration changes.
     */
    public JourneyArc adjustConstantRegion(double newDistance) {
        Preconditions.checkArgument(
            newDistance >= getShortestPossibleArc(),
            "Cannot produce %s, an arc shorter than %s", newDistance, getShortestPossibleArc());

        var dir = getDirection();
        var deltaHeight = (dir == DirectionOfTravel.GOING_UP)   ? newDistance - distance()
                        : (dir == DirectionOfTravel.GOING_DOWN) ? distance() - newDistance
                        : 0.0;
        var deltaCVDuration = deltaHeight / this.constantVelocity.getFinalVelocity();
        var newConstantDuration = this.constantVelocity.getDuration() + deltaCVDuration;

        return this.toBuilder()
            .constantVelocity(this.constantVelocity.withNewDuration(newConstantDuration))
            .reverseJerkTwo(spaceTimeShift(this.reverseJerkTwo, deltaHeight, deltaCVDuration))
            .forwardJerkTwo(spaceTimeShift(this.forwardJerkTwo, deltaHeight, deltaCVDuration))
            .terminalSegment(spaceTimeShift(this.terminalSegment, deltaHeight, deltaCVDuration))
            .terminalMoment(this.terminalMoment.copy(it ->
                it.time(this.terminalMoment.getTime() + deltaCVDuration)
                  .height(this.terminalMoment.getHeight() + deltaHeight)))
            .build();
    }


    public JourneyArc moveEndpointsByOffset(double offset) {
        return this.toBuilder()
            .initialMoment(heightOffset(this.initialMoment, offset))
            .forwardJerkOne(heightOffset(this.forwardJerkOne, offset))
            .forwardAcceleration(heightOffset(this.forwardAcceleration, offset))
            .reverseJerkOne(heightOffset(this.reverseJerkOne, offset))
            .constantVelocity(heightOffset(this.constantVelocity, offset))
            .reverseJerkTwo(heightOffset(this.reverseJerkTwo, offset))
            .forwardJerkTwo(heightOffset(this.forwardJerkTwo, offset))
            .terminalSegment(heightOffset(this.terminalSegment, offset))
            .terminalMoment(heightOffset(this.terminalMoment, offset))
            .build();
    }


    public static JourneyArc fromList(List<IPathLeg> list) {
        return JourneyArc.build(bldr ->
            bldr.initialMoment(list.get(0).getInitialMoment())
                .forwardJerkOne((ConstantJerkPathLeg) list.get(0))
                .forwardAcceleration((ConstantAccelerationPathLeg) list.get(1))
                .reverseJerkOne((ConstantJerkPathLeg) list.get(2))
                .constantVelocity((ConstantVelocityPathLeg) list.get(3))
                .reverseJerkTwo((ConstantJerkPathLeg) list.get(4))
                .forwardJerkTwo((ConstantJerkPathLeg) list.get(5))
                .terminalSegment((ConstantJerkPathLeg) list.get(6))
                .terminalMoment(list.get(6).getFinalMoment(0))
        );
    }


    // -------------------------------------------------------------------------
    // Private helpers (were Xtend extension methods on specific leg types)
    // -------------------------------------------------------------------------

    private static ConstantJerkPathLeg spaceTimeShift(
            ConstantJerkPathLeg leg, double deltaHeight, double deltaTime) {
        return new ConstantJerkPathLeg(
            leg.getInitialMoment().copy(it ->
                it.time(leg.getInitialTime() + deltaTime)
                  .height(leg.getInitialHeight() + deltaHeight)),
            leg.getDuration()
        );
    }

    private static ConstantAccelerationPathLeg spaceTimeShift(
            ConstantAccelerationPathLeg leg, double deltaHeight, double deltaTime) {
        return new ConstantAccelerationPathLeg(
            leg.getInitialMoment().copy(it ->
                it.time(leg.getInitialTime() + deltaTime)
                  .height(leg.getInitialHeight() + deltaHeight)),
            leg.getDuration()
        );
    }

    private static ConstantJerkPathLeg heightOffset(ConstantJerkPathLeg leg, double offset) {
        return new ConstantJerkPathLeg(heightOffset(leg.getInitialMoment(), offset), leg.getDuration());
    }

    private static ConstantAccelerationPathLeg heightOffset(ConstantAccelerationPathLeg leg, double offset) {
        return new ConstantAccelerationPathLeg(heightOffset(leg.getInitialMoment(), offset), leg.getDuration());
    }

    private static ConstantVelocityPathLeg heightOffset(ConstantVelocityPathLeg leg, double offset) {
        return new ConstantVelocityPathLeg(heightOffset(leg.getInitialMoment(), offset), leg.getDuration());
    }

    private static PathMoment heightOffset(PathMoment moment, double offset) {
        return moment.copy(it -> it.height(moment.getHeight() + offset));
    }
}
