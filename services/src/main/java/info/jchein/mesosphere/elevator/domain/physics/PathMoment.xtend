package info.jchein.mesosphere.elevator.domain.physics

import org.eclipse.xtend.lib.annotations.Data
import de.oehme.xtend.contrib.Buildable

@Data
@Buildable
public class PathMoment {
    val double time
    val double height
    val double velocity
    val double acceleration
    val double jerk
}
