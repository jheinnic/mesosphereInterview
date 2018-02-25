package fixtures.elevator.emulator.model;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import fixtures.elevator.runtime.temporal.VirtualRuntimeTestConfiguration;
import info.jchein.mesosphere.elevator.common.bootstrap.BootstrapConfiguration;


@TestConfiguration
@ContextConfiguration(classes = {
   BootstrapConfiguration.class, VirtualRuntimeTestConfiguration.class,
   EmulatorBootstrapTestConfiguration.class
}, loader = AnnotationConfigContextLoader.class)
@TestPropertySource("classpath:/fixtures/elevator/emulator/model/EmulatorBootstrapTestConfiguration.properties")
@ActiveProfiles("elevator.runtime.virtual")
public class EmulatorBootstrapTestConfiguration
{

}
