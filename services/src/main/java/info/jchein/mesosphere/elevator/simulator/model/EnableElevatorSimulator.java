package info.jchein.mesosphere.elevator.simulator.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import info.jchein.mesosphere.elevator.control.model.EnableElevatorController;
import info.jchein.mesosphere.elevator.emulator.model.EnableEmulatedElevator;
import info.jchein.mesosphere.elevator.runtime.virtual.EnableVirtualRuntime;

// Enable the dependent modules: Elevator Control, Virtual Time, and the software Elevator Emulator.
@EnableElevatorController
@EnableVirtualRuntime
@EnableEmulatedElevator
// Add the Simulation core components
@Import(ElevatorSimulationConfiguration.class)
// And scan the location for profile-dependent workload defiitions to bootstrap.  There should be one simulation profile
// in the active set to determine which worklad's contents to create and manage.
@ComponentScan("info.jchein.mesosphere.elevator.simulator.workloads")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableElevatorSimulator
{

}
