package info.jchein.mesosphere.domain.subsystem;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.domain.factory.IDirector;
import info.jchein.mesosphere.domain.factory.IImmutableListBuilder;

public interface IPortDriverFactory<Port, Driver, PortBuilder>
{
	ImmutableList<? extends Driver> buildPortDrivers( ImmutableList.Builder<Port> portListBuilder, IDirector<IImmutableListBuilder<PortBuilder>> director);
}
