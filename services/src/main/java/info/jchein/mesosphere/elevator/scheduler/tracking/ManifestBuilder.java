package info.jchein.mesosphere.elevator.scheduler.tracking;

import java.util.BitSet;

public interface ManifestBuilder {
	ManifestBuilder addRecentPickup( long arrivalTime, int originFloor, BitSet potentialDropofFloors );
}
