package info.jchein.mesosphere.domain.subsystem;

import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.domain.factory.IListBuilder;

public interface IPortDriverFactory<Port, Driver, PortBuilder>
{
	ImmutableList<? extends Driver> buildPortDrivers( ImmutableList.Builder<Port> portListBuilder, Consumer<IListBuilder<PortBuilder>> director);
}
