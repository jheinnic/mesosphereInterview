package fixtures.elevator.runtime.temporal;


// import org.junit.runner.RunWith;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import info.jchein.mesosphere.elevator.common.IdentityConfiguration;
import info.jchein.mesosphere.elevator.common.bootstrap.BootstrapConfiguration;
import info.jchein.mesosphere.elevator.runtime.event.EventBusConfiguration;
import info.jchein.mesosphere.elevator.runtime.temporal.VirtualTimerConfiguration;


@TestConfiguration
@ContextConfiguration(classes= {BootstrapConfiguration.class, VirtualRuntimeTestConfiguration.class}, loader=AnnotationConfigContextLoader.class)
@TestPropertySource("classpath:/fixtures/elevator/runtime/temporal/VirtualRuntimeTestConfiguration.properties")
@ActiveProfiles("elevator.runtime.virtual")
public class VirtualRuntimeTestConfiguration
{

}
