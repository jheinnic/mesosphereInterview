package info.jchein.mesosphere.elevator.common.physics;

import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.bootstrap.BuildingDescription;
import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentConfiguration;
import info.jchein.mesosphere.elevator.common.bootstrap.DoorTimeDescription;
import info.jchein.mesosphere.elevator.common.bootstrap.StartStopDescription;
import info.jchein.mesosphere.elevator.common.bootstrap.TravelSpeedDescription;
import info.jchein.mesosphere.elevator.common.bootstrap.WeightDescription;
import lombok.ToString;
import org.springframework.util.Assert;


@ToString
public abstract class ElevatorPhysicsService implements IElevatorPhysicsService {

    private final BuildingDescription buildingProps;
    private final StartStopDescription motorProps;
    private final TravelSpeedDescription speedProps;
    private final DoorTimeDescription doorProps;
    private final WeightDescription weightProps;

    private final int numFloors;
    private final double metersPerFloor;
    private final double maxJerk;
    private final double minJerk;
    private final double maxAccel;
    private final double minAccel;

    private final double distBrk;
    private final double upSpeedBrk;
    private final double downSpeedBrk;

    private final double tMaxA;
    private final double vMaxA;
    private final double dMaxA;

    private final double tStopBrk;
    private final double maxAccelBrake;
    private final double minAccelBrake;

    private final int minFastDistance;

    private final JourneyArc slowAscent;
    private final JourneyArc slowDescent;
    private final JourneyArc fastAscent;
    private final JourneyArc fastDescent;

    private final ConcurrentHashMap<Long, JourneyArc> traversalCache = new ConcurrentHashMap<>();


    protected ElevatorPhysicsService(DeploymentConfiguration deploymentConfiguration) {
        this.buildingProps = deploymentConfiguration.getBuilding();
        this.motorProps    = deploymentConfiguration.getMotor();
        this.speedProps    = deploymentConfiguration.getTopSpeed();
        this.doorProps     = deploymentConfiguration.getDoors();
        this.weightProps   = deploymentConfiguration.getWeight();

        this.numFloors      = this.buildingProps.getNumFloors();
        this.metersPerFloor = this.buildingProps.getMetersPerFloor();
        this.maxJerk        = this.motorProps.getMaxJerk();
        this.minJerk        = -1 * this.maxJerk;
        this.maxAccel       = this.motorProps.getMaxAcceleration();
        this.minAccel       = -1 * this.maxAccel;
        this.upSpeedBrk     = this.motorProps.getBrakeSpeed();
        this.downSpeedBrk   = -1 * this.upSpeedBrk;
        this.distBrk        = this.motorProps.getBrakeDistance();

        this.tMaxA = this.maxAccel / this.maxJerk;
        this.vMaxA = this.maxJerk * this.tMaxA * this.tMaxA / 2.0;
        this.dMaxA = this.vMaxA * this.tMaxA / 3.0;

        this.maxAccelBrake = this.upSpeedBrk * this.upSpeedBrk / (2 * this.distBrk);
        this.tStopBrk      = this.upSpeedBrk / this.maxAccelBrake;
        this.minAccelBrake = -1 * this.maxAccelBrake;

        Preconditions.checkArgument(
            this.maxAccelBrake < this.maxAccel,
            "It must be possible to brake from the stopping distance at constant rate of velocity change");
        Preconditions.checkArgument(
            this.minAccelBrake > this.minAccel,
            "It must be possible to brake from the stopping distance at constant rate of velocity change");

        var floorHeight    = this.metersPerFloor;
        var lastFloorPair  = this.numFloors - 1;
        var buildingHeight = floorHeight * lastFloorPair;

        this.slowAscent  = computeUpwardArch(
            PathMoment.build(it -> it.time(0).height(0).velocity(0).acceleration(0).jerk(0)),
            this.speedProps.getShortHop());
        this.slowDescent = computeDownwardArch(
            PathMoment.build(it -> it.time(0).height(buildingHeight).velocity(0).acceleration(0).jerk(0)),
            -1 * this.speedProps.getShortHop());
        this.fastAscent  = computeUpwardArch(
            PathMoment.build(it -> it.time(0).height(0).velocity(0).acceleration(0).jerk(0)),
            this.speedProps.getLongAscent());
        this.fastDescent = computeDownwardArch(
            PathMoment.build(it -> it.time(0).height(buildingHeight).velocity(0).acceleration(0).jerk(0)),
            this.speedProps.getLongDescent());

        for (IPathLeg nextLeg : this.slowAscent) {
            System.out.println(nextLeg.toString());
        }

        Assert.isTrue(this.slowDescent.getShortestPossibleArc() <= floorHeight,
            "Must be able to traverse one floor within slow speed arc's path");

        int ii = 0;
        double nextHeight    = floorHeight;
        double fastArcDistance = this.fastAscent.getShortestPossibleArc();

        for (; ii < lastFloorPair && nextHeight < fastArcDistance; ii++) {
            System.out.println(String.format("%d is slow", ii));
            nextHeight += floorHeight;
        }
        this.minFastDistance = ii + 1;

        for (int jj = ii; jj < lastFloorPair; jj++) {
            System.out.println(String.format("%d is fast", jj));
            nextHeight += floorHeight;
        }
    }


    // -------------------------------------------------------------------------
    // Private helpers (were Xtend extension methods on IPathLeg)
    // -------------------------------------------------------------------------

    private PathMoment nextMoment(
            IPathLeg pathLeg, ImmutableList.Builder<IPathLeg> listBuilder, double nextJerk) {
        listBuilder.add(pathLeg);
        return pathLeg.getFinalMoment(nextJerk);
    }

    private ImmutableList<IPathLeg> endPath(
            IPathLeg finalLeg, ImmutableList.Builder<IPathLeg> listBuilder) {
        listBuilder.add(finalLeg);
        return listBuilder.build();
    }


    // -------------------------------------------------------------------------
    // Arc computation
    // -------------------------------------------------------------------------

    private JourneyArc computeUpwardArch(PathMoment moment, double _maxSpeed) {
        var listBuilder = ImmutableList.<IPathLeg>builder();
        var maxSpeed    = (_maxSpeed > 0) ? _maxSpeed : -1 * _maxSpeed;

        var tJerkUpOne  = (this.maxAccel - moment.getAcceleration()) / this.maxJerk;
        var toMaxUpAcc  = new ConstantJerkPathLeg(moment.copy(it -> it.jerk(this.maxJerk)), tJerkUpOne);

        var vAtZeroAcc = toMaxUpAcc.getFinalVelocity()
            + (this.maxAccel * this.tMaxA)
            + (this.minJerk * this.tMaxA * this.tMaxA / 2.0);

        ConstantVelocityPathLeg toJerkDownTwo;
        if (vAtZeroAcc > maxSpeed) {
            tJerkUpOne = Math.sqrt(maxSpeed / this.maxJerk);
            toMaxUpAcc = new ConstantJerkPathLeg(moment.copy(it -> it.jerk(this.maxJerk)), tJerkUpOne);
            var atMaxUpAcc    = nextMoment(toMaxUpAcc, listBuilder, 0.0);
            var toJerkDownOne = new ConstantAccelerationPathLeg(atMaxUpAcc, 0);
            var atJerkDownOne = nextMoment(toJerkDownOne, listBuilder, this.minJerk);
            var toConstV      = new ConstantJerkPathLeg(atJerkDownOne, tJerkUpOne);
            var atConstV      = nextMoment(toConstV, listBuilder, 0.0);
            toJerkDownTwo = new ConstantVelocityPathLeg(atConstV, 0);
        } else {
            var atMaxUpAcc = nextMoment(toMaxUpAcc, listBuilder, 0.0);
            Assert.isTrue(atMaxUpAcc.getAcceleration() == this.maxAccel && atMaxUpAcc.getVelocity() > 0,
                "Acceleration must reach maximum");

            var tJerkDownOne  = atMaxUpAcc.getAcceleration() / this.maxJerk;
            var vJerkDownOne  = maxSpeed
                - (tJerkDownOne * atMaxUpAcc.getAcceleration())
                - (this.minJerk * tJerkDownOne * tJerkDownOne / 2.0);
            var tMaxUpAcc     = (vJerkDownOne - atMaxUpAcc.getVelocity()) / atMaxUpAcc.getAcceleration();

            var toJerkDownOne = new ConstantAccelerationPathLeg(atMaxUpAcc, tMaxUpAcc);
            var atJerkDownOne = nextMoment(toJerkDownOne, listBuilder, this.minJerk);
            var toConstV      = new ConstantJerkPathLeg(atJerkDownOne, tJerkDownOne);
            var atConstV      = nextMoment(toConstV, listBuilder, 0.0);
            toJerkDownTwo = new ConstantVelocityPathLeg(atConstV, 0);
        }

        var brakingSolver  = getBrakingSolver(this.distBrk, this.upSpeedBrk, toJerkDownTwo.getFinalVelocity(), this.maxJerk);
        var atPreBrakeZero = nextMoment(toJerkDownTwo, listBuilder, brakingSolver.getJerkZero());
        var toPreBrakeOne  = new ConstantJerkPathLeg(atPreBrakeZero, brakingSolver.getTimeZero());
        var atPreBrakeOne  = nextMoment(toPreBrakeOne, listBuilder, brakingSolver.getJerkOne());
        var toBrakes       = new ConstantJerkPathLeg(atPreBrakeOne, brakingSolver.getTimeOne());
        var atBrakes       = nextMoment(toBrakes, listBuilder, brakingSolver.getJerkTwo());

        System.out.println(String.format("%f %f", brakingSolver.getTimeTwo(), this.tStopBrk));
        return JourneyArc.fromList(
            endPath(new ConstantJerkPathLeg(atBrakes, brakingSolver.getTimeTwo()), listBuilder));
    }


    private JourneyArc computeDownwardArch(PathMoment moment, double _minSpeed) {
        var listBuilder = ImmutableList.<IPathLeg>builder();
        var minSpeed    = (_minSpeed < 0) ? _minSpeed : -1 * _minSpeed;

        var tJerkDownOne  = (this.minAccel - moment.getAcceleration()) / this.minJerk;
        IPathLeg toMaxDownAcc = new ConstantJerkPathLeg(moment.copy(it -> it.jerk(this.minJerk)), tJerkDownOne);

        var vAtZeroAcc = ((ConstantJerkPathLeg) toMaxDownAcc).getFinalVelocity()
            + (this.minAccel * this.tMaxA)
            + (this.maxJerk * this.tMaxA * this.tMaxA / 2.0);

        ConstantVelocityPathLeg toJerkUpTwo;
        if (vAtZeroAcc < minSpeed) {
            tJerkDownOne  = Math.sqrt(minSpeed / this.minJerk);
            toMaxDownAcc  = new ConstantJerkPathLeg(moment.copy(it -> it.jerk(this.minJerk)), tJerkDownOne);
            var atMaxDownAcc  = nextMoment(toMaxDownAcc, listBuilder, 0.0);
            var toJerkUpOne   = new ConstantAccelerationPathLeg(atMaxDownAcc, 0);
            var atJerkUpOne   = nextMoment(toJerkUpOne, listBuilder, this.maxJerk);
            var toConstV      = new ConstantJerkPathLeg(atJerkUpOne, tJerkDownOne);
            var atConstV      = nextMoment(toConstV, listBuilder, 0.0);
            toJerkUpTwo = new ConstantVelocityPathLeg(atConstV, 0);
        } else {
            var atMaxDownAcc  = nextMoment(toMaxDownAcc, listBuilder, 0.0);
            var tJerkUpOne    = atMaxDownAcc.getAcceleration() / this.minJerk;
            var vJerkUpOne    = minSpeed
                - (tJerkUpOne * atMaxDownAcc.getAcceleration())
                - (this.maxJerk * tJerkUpOne * tJerkUpOne / 2.0);
            var tMaxDownAcc   = (vJerkUpOne - atMaxDownAcc.getVelocity()) / atMaxDownAcc.getAcceleration();
            var toJerkUpOne   = new ConstantAccelerationPathLeg(atMaxDownAcc, tMaxDownAcc);
            var atJerkUpOne   = nextMoment(toJerkUpOne, listBuilder, this.maxJerk);
            var toConstV      = new ConstantJerkPathLeg(atJerkUpOne, tJerkUpOne);
            var atConstV      = nextMoment(toConstV, listBuilder, 0.0);
            toJerkUpTwo = new ConstantVelocityPathLeg(atConstV, 0);
        }

        var brakingSolver  = getBrakingSolver(this.distBrk, this.downSpeedBrk, toJerkUpTwo.getFinalVelocity(), this.maxJerk);
        var atPreBrakeZero = nextMoment(toJerkUpTwo, listBuilder, brakingSolver.getJerkZero());
        var toPreBrakeOne  = new ConstantJerkPathLeg(atPreBrakeZero, brakingSolver.getTimeZero());
        var atPreBrakeOne  = nextMoment(toPreBrakeOne, listBuilder, brakingSolver.getJerkOne());
        var toBrakes       = new ConstantJerkPathLeg(atPreBrakeOne, brakingSolver.getTimeOne());
        var atBrakes       = nextMoment(toBrakes, listBuilder, brakingSolver.getJerkTwo());

        System.out.println(String.format("%f %f", brakingSolver.getTimeTwo(), this.tStopBrk));
        return JourneyArc.fromList(
            endPath(new ConstantJerkPathLeg(atBrakes, brakingSolver.getTimeTwo()), listBuilder));
    }


    protected abstract IBrakingSolver getBrakingSolver(
        double brakeDistance, double brakeVelocity, double initialVelocity, double maxJerk);


    // -------------------------------------------------------------------------
    // IElevatorPhysicsService implementation
    // -------------------------------------------------------------------------

    @Override
    public boolean isTravelFast(int fromFloorIndex, int toFloorIndex) {
        if (fromFloorIndex > toFloorIndex) {
            return (fromFloorIndex - toFloorIndex) >= this.minFastDistance;
        } else if (fromFloorIndex < toFloorIndex) {
            return (toFloorIndex - fromFloorIndex) >= this.minFastDistance;
        }
        return false;
    }

    @Override
    public double getExpectedStopDuration(int boardingCount, int disembarkingCount) {
        return this.doorProps.getOpenCloseTime()
            + Math.max(this.doorProps.getMinHold(),
                this.doorProps.getPersonHold() * (boardingCount + disembarkingCount));
    }

    @Override
    public int getIdealPassengerCount() {
        return (int) Math.floor(
            (this.weightProps.getMaxWeightAllowed() * this.weightProps.getPctMaxForIdeal())
            / this.weightProps.getAvgPassenger());
    }

    @Override
    public int getMaxTolerancePassengerCount() {
        return (int) Math.floor(
            (this.weightProps.getMaxWeightAllowed() * this.weightProps.getPctMaxForPickup())
            / this.weightProps.getAvgPassenger());
    }

    @Override
    public double getFloorDistance(int fromFloorIndex, int toFloorIndex) {
        return Math.abs(toFloorIndex - fromFloorIndex) * this.buildingProps.getMetersPerFloor();
    }

    @Override
    public double getTravelTime(int fromFloorIndex, int toFloorIndex) {
        Preconditions.checkArgument(fromFloorIndex != toFloorIndex,
            "to and from floor indices cannot both be <%s> and <%s>", fromFloorIndex, toFloorIndex);
        return doGetTraversalPath(fromFloorIndex, toFloorIndex).getDuration();
    }

    @Override
    public JourneyArc getTraversalPath(int fromFloorIndex, int toFloorIndex) {
        return doGetTraversalPath(fromFloorIndex, toFloorIndex);
    }

    protected JourneyArc doGetTraversalPath(int fromFloorIndex, int toFloorIndex) {
        long key = ((long) fromFloorIndex << 32) | ((long) toFloorIndex & 0xFFFFFFFFL);
        return traversalCache.computeIfAbsent(key, k -> computeTraversalPath(fromFloorIndex, toFloorIndex));
    }

    private JourneyArc computeTraversalPath(int fromFloorIndex, int toFloorIndex) {
        var fastTravel       = isTravelFast(fromFloorIndex, toFloorIndex);
        var absoluteDistance = getFloorDistance(fromFloorIndex, toFloorIndex);
        double pathShift;
        JourneyArc baseOriginPath;

        if (fromFloorIndex < toFloorIndex) {
            pathShift      = fromFloorIndex * this.metersPerFloor;
            baseOriginPath = fastTravel ? this.fastAscent : this.slowAscent;
        } else {
            pathShift      = (1 - this.numFloors + fromFloorIndex) * this.metersPerFloor;
            baseOriginPath = fastTravel ? this.fastDescent : this.slowDescent;
        }

        return baseOriginPath.adjustConstantRegion(absoluteDistance).moveEndpointsByOffset(pathShift);
    }

    public double travelDuration(ImmutableList<IPathLeg> path) {
        return path.get(path.size() - 1).getFinalTime() - path.get(0).getInitialTime();
    }

    public double travelDistance(ImmutableList<IPathLeg> path) {
        var displacement = path.get(path.size() - 1).getFinalHeight() - path.get(0).getInitialHeight();
        return displacement < 0 ? displacement * -1 : displacement;
    }

    @Override
    public int getNumFloors() {
        return this.numFloors;
    }

    @Override
    public double getMetersPerFloor() {
        return this.metersPerFloor;
    }

    @Override
    public int getNumElevators() {
        throw new UnsupportedOperationException("TODO: numElevators not tracked by ElevatorPhysicsService");
    }

    @Override
    public double getSensorHeight(int floorIndex, DirectionOfTravel direction) {
        throw new UnsupportedOperationException("TODO: auto-generated method stub");
    }
}
