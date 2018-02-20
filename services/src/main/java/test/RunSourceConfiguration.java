package test;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import info.jchein.mesosphere.elevator.common.IdentityConfiguration;
import info.jchein.mesosphere.elevator.common.bootstrap.BootstrapConfiguration;
import info.jchein.mesosphere.elevator.common.graph.GraphUtilsConfiguration;
import info.jchein.mesosphere.elevator.common.physics.PhysicsUtilsConfiguration;
import info.jchein.mesosphere.elevator.common.probability.ProbabilityUtilsConfiguration;
import info.jchein.mesosphere.elevator.control.model.ElevatorControlConfiguration;
import info.jchein.mesosphere.elevator.emulator.model.EmulatedElevatorDriverConfiguration;
import info.jchein.mesosphere.elevator.runtime.event.EventBusConfiguration;
import info.jchein.mesosphere.elevator.runtime.temporal.VirtualRuntimeConfiguration;
import info.jchein.mesosphere.elevator.simulator.model.ElevatorSimulationConfiguration;


@Configuration
// @EnableElevatorSimulator
@Import({
   RunSourceRunner.class, IdentityConfiguration.class, BootstrapConfiguration.class,
   ProbabilityUtilsConfiguration.class, PhysicsUtilsConfiguration.class, GraphUtilsConfiguration.class,
   ElevatorControlConfiguration.class, ElevatorSimulationConfiguration.class,
   EventBusConfiguration.class, EmulatedElevatorDriverConfiguration.class,
   VirtualRuntimeConfiguration.class,
})
public class RunSourceConfiguration
{}
