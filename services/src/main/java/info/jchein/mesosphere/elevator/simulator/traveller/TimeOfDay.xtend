package info.jchein.mesosphere.elevator.simulator.traveller

import com.google.common.collect.ImmutableList
import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data

@Data
@Buildable
class TimeOfDay {
	val double inSystemMean
	val double inSystemStdDev
	val ImmutableList<ActivityDescriptor> activities;
}
