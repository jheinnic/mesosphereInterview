package info.jchein.mesosphere.configuartion.tests;

//import org.junit.runner.RunWith;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;

import info.jchein.mesosphere.elevator.runtime.virtual.EnableVirtualRuntime;

@TestConfiguration
@EnableVirtualRuntime
@ActiveProfiles("elevator.runtime.virtual")
public class VirtualRuntimeTestConfiguration
{

}
