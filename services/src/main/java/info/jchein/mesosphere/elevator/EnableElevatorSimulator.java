package info.jchein.mesosphere.elevator;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import info.jchein.mesosphere.elevator.common.IdentityConfiguration;
import info.jchein.mesosphere.elevator.common.bootstrap.BootstrapConfiguration;
import info.jchein.mesosphere.elevator.common.graph.GraphUtilsConfiguration;
import info.jchein.mesosphere.elevator.common.physics.PhysicsUtilsConfiguration;
import info.jchein.mesosphere.elevator.common.probability.ProbabilityUtilsConfiguration;
import info.jchein.mesosphere.elevator.control.ElevatorControlConfiguration;
import info.jchein.mesosphere.elevator.emulator.model.EmulatorDriverConfiguration;
import info.jchein.mesosphere.elevator.runtime.event.EventBusConfiguration;
import info.jchein.mesosphere.elevator.runtime.temporal.VirtualTimerConfiguration;
import info.jchein.mesosphere.elevator.simulator.model.ElevatorSimulationConfiguration;


@Import({
   BootstrapConfiguration.class, GraphUtilsConfiguration.class, PhysicsUtilsConfiguration.class,
   ProbabilityUtilsConfiguration.class, ElevatorControlConfiguration.class,
   EmulatorDriverConfiguration.class, ElevatorSimulationConfiguration.class,
   EventBusConfiguration.class, VirtualTimerConfiguration.class, IdentityConfiguration.class
})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableElevatorSimulator
{

}
