package info.jchein.mesosphere.elevator.simulator.model

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data

@Data
@Buildable
class TripLogEntry {
	long callTime
	long boardingTime
	int boardingFloorIndex
	long dismebarkTime
	int disembarkFloorIndex
}
