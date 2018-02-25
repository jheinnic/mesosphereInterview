package info.jchein.mesosphere.elevator.simulator.model;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


//And scan the location for profile-dependent workload defiitions to bootstrap.  There should be one simulation profile
//in the active set to determine which worklad's contents to create and manage.
@ComponentScan({"info.jchein.mesosphere.elevator.simulator.workloads", "info.jchein.mesosphere.elevator.simulator.model", "info.jchein.mesosphere.elevator.simulator.passengers"})
@Configuration
public class ElevatorSimulationConfiguration {
}
