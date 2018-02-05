package info.jchein.mesosphere.configuartion.tests;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.TestPropertySource;

import info.jchein.mesosphere.elevator.runtime.EnableElevatorRuntime;

@TestConfiguration
@TestPropertySource
@EnableElevatorRuntime
public class FrameworkRuntimeTestConfiguration
{

}
