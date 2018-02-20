package info.jchein.mesosphere.test.config.runtime.temporal;


// import org.junit.runner.RunWith;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import info.jchein.mesosphere.elevator.common.IdentityConfiguration;
import info.jchein.mesosphere.elevator.common.bootstrap.BootstrapConfiguration;
import info.jchein.mesosphere.elevator.runtime.event.EventBusConfiguration;
import info.jchein.mesosphere.elevator.runtime.temporal.VirtualRuntimeConfiguration;


@TestConfiguration
@EnableTestVirtualRuntime
public class VirtualRuntimeTestConfiguration
{

}
