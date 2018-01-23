package info.jchein.mesosphere.elevator.domain.common

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data
import com.google.common.collect.ImmutableList
import info.jchein.mesosphere.elevator.domain.common.PickupCall
import info.jchein.mesosphere.elevator.domain.common.ElevatorCarSnapshot

@Data
@Buildable
class ElevatorGroupSnapshot {
	val ImmutableList<PickupCall> pendingPickups
	val ImmutableList<ElevatorCarSnapshot> carSnapshots
}
