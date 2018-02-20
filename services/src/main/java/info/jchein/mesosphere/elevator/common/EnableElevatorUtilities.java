package info.jchein.mesosphere.elevator.common;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;


@ComponentScan({
   "info.jchein.mesosphere.elevator.common.bootstrap",
   "info.jchein.mesosphere.elevator.common.graph",
   "info.jchein.mesosphere.elevator.common.physics",
   "info.jchein.mesosphere.elevator.common.probability"
})
@Import(IdentityConfiguration.class)
public @interface EnableElevatorUtilities
{

}
