package info.jchein.mesosphere.test.config.runtime.temporal;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Configuration;

import info.jchein.mesosphere.elevator.control.model.EnableElevatorControl;
import info.jchein.mesosphere.elevator.emulator.model.EnableEmulatedElevator;
import info.jchein.mesosphere.elevator.runtime.temporal.temporal.EnableVirtualRuntime;

@TestConfiguration
@EnableEmulatedElevator
@EnableElevatorControl
@EnableVirtualRuntime
public class EmulatorBootstrapTestConfiguration
{

}
