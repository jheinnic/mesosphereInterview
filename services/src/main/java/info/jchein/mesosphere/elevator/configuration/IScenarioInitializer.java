package info.jchein.mesosphere.elevator.configuration;

import java.util.function.Consumer;

import info.jchein.mesosphere.elevator.domain.common.ElevatorGroupBootstrap.ElevatorGroupBootstrapBuilder;

@FunctionalInterface
public interface IScenarioInitializer extends Consumer<ElevatorGroupBootstrapBuilder>
{

}
