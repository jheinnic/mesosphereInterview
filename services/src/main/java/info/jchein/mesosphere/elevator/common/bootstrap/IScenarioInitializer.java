package info.jchein.mesosphere.elevator.common.bootstrap;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.common.bootstrap.ElevatorGroupBootstrap.ElevatorGroupBootstrapBuilder;

@FunctionalInterface
public interface IScenarioInitializer extends Consumer<ElevatorGroupBootstrapBuilder>
{

}
