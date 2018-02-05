package info.jchein.mesosphere.elevator.emulator.physics;

import java.util.Iterator;
import java.util.NoSuchElementException;

class JourneyArcPathLegIterator implements Iterator<IPathLeg> {
  private final JourneyArc source;
  private JourneyArcPathLegRole nextRole;
  
  JourneyArcPathLegIterator(final JourneyArc source) {
    this.source = source;
    this.nextRole = JourneyArcPathLegRole.FORWARD_JERK_ONE;
  }
  
  @Override
  public boolean hasNext() {
    return (this.nextRole != null);
  }
  
  @Override
  public IPathLeg next() {
    try {
      switch (this.nextRole) {
        case FORWARD_JERK_ONE: return this.source.getForwardJerkOne();
        case FORWARD_ACCELERATION: return this.source.getForwardAcceleration();
        case REVERSE_JERK_ONE: return this.source.getReverseJerkOne();
        case CONSTANT_VELOCITY: return this.source.getConstantVelocity();
        case REVERSE_JERK_TWO: return this.source.getReverseJerkTwo();
        case REVERSE_ACCELERATION: return this.source.getReverseAcceleration();
        case FORWARD_JERK_TWO: return this.source.getForwardJerkTwo();
        case TERMINAL_SEGMENT: return this.source.getTerminalSegment();
        default: throw new NoSuchElementException();
      }
    } finally {
      this.nextRole = (this.nextRole != null) ? this.nextRole.getNextLeg() : null;
    }
  }
}
