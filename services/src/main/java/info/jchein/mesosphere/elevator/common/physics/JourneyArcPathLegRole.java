package info.jchein.mesosphere.elevator.common.physics;

import java.util.EnumMap;

public enum JourneyArcPathLegRole {
	FORWARD_JERK_ONE,
	FORWARD_ACCELERATION,
	REVERSE_JERK_ONE,
	CONSTANT_VELOCITY,
	REVERSE_JERK_TWO,
	FORWARD_JERK_TWO,
	TERMINAL_SEGMENT;
	
	private static final EnumMap<JourneyArcPathLegRole, JourneyArcPathLegRole> ARC_PATH_ORDER =
		new EnumMap<JourneyArcPathLegRole, JourneyArcPathLegRole>(JourneyArcPathLegRole.class);
	
	static {
		ARC_PATH_ORDER.put(FORWARD_JERK_ONE, FORWARD_ACCELERATION);
		ARC_PATH_ORDER.put(FORWARD_ACCELERATION, REVERSE_JERK_ONE);
		ARC_PATH_ORDER.put(REVERSE_JERK_ONE, CONSTANT_VELOCITY);
		ARC_PATH_ORDER.put(CONSTANT_VELOCITY, REVERSE_JERK_TWO);
		ARC_PATH_ORDER.put(REVERSE_JERK_TWO, FORWARD_JERK_TWO);
		ARC_PATH_ORDER.put(FORWARD_JERK_TWO, TERMINAL_SEGMENT);
	}
	
	JourneyArcPathLegRole getNextLeg() {
		return ARC_PATH_ORDER.get(this);
	}
}
