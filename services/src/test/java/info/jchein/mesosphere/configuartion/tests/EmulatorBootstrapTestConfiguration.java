package info.jchein.mesosphere.configuartion.tests;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Configuration;

import info.jchein.mesosphere.elevator.control.model.EnableElevatorController;
import info.jchein.mesosphere.elevator.emulator.model.EnableEmulatedElevator;
import info.jchein.mesosphere.elevator.runtime.virtual.EnableVirtualRuntime;

@TestConfiguration
@EnableEmulatedElevator
@EnableElevatorController
@EnableVirtualRuntime
public class EmulatorBootstrapTestConfiguration
{

}
