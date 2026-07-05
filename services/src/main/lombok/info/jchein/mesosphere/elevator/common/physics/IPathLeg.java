package info.jchein.mesosphere.elevator.common.physics;

import java.util.Iterator;

import info.jchein.mesosphere.elevator.common.physics.PathMoment;

public interface IPathLeg {
    DisplacementFormulaType getDisplacementFormulaType();	
    double getInitialTime();
    double getFinalTime();
    double getJerk();

    double getInitialAcceleration();
    double getFinalAcceleration();
    double getInitialVelocity();
    double getFinalVelocity();
    double getInitialHeight();
    double getFinalHeight();
    
    /**
     * Delta height over the duration of a path leg.  Unlike JourneyArc.distance, this application of the term does not refer to an absolute 
     * value.  Whereas JourneyArc will report distance of a descent using positive values, its individual path legs should all report negative
     * values for their distance metrics.
     * 
     * @return
     */
    double getDistance();
    double getDuration();
    
    PathMoment getInitialMoment();
    PathMoment getFinalMoment(double nextJerk);
    
    Iterator<PathMoment> toMomentIterator(double tickDuration, double firstTick);
    IPathLeg truncate(double fromTime);
}
