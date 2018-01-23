package info.jchein.mesosphere.elevator.simulator.traveller

import de.oehme.xtend.contrib.Buildable
import org.eclipse.xtend.lib.annotations.Data
import org.hibernate.validator.constraints.Range

@Data
@Buildable
class LocationCdf {
	@Range(min=0, max=1)
	val double cdfMaxFloorA;

	@Range(min=0, max=1)
	val double cdfMaxFloorB;

	@Range(min=0, max=1)
	val double cdfMaxFloorC;

	@Range(min=0, max=1)
	val double cdfMaxLobby;
}
