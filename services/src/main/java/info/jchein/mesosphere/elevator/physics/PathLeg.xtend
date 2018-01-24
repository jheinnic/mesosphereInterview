package info.jchein.mesosphere.elevator.physics

import java.util.Iterator

interface PathLeg {
    def PathLegType getLegType();	
    def double getInitialTime();
    def double getFinalTime();
    def double getJerk();

    def double getInitialAcceleration();
    def double getFinalAcceleration();
    def double getInitialVelocity();
    def double getFinalVelocity();
    def double getInitialHeight();
    def double getFinalHeight();

    def double getDuration();
    def double getDistance();
    
    def PathMoment getInitialMoment();
    def PathMoment getFinalMoment(double nextJerk);
    
    def Iterator<PathMoment> momentIterator(double tickDuration);
}