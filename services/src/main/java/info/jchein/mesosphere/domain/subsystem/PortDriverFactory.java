package info.jchein.mesosphere.domain.subsystem;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import info.jchein.mesosphere.domain.factory.IDirector;
import info.jchein.mesosphere.domain.factory.IFactory;
import info.jchein.mesosphere.domain.factory.IImmutableListBuilder;
import info.jchein.mesosphere.domain.factory.ImmutableListBuilderSupplier;

/**
 * Reusable Factory implementation that Bridges strategies for Port and Drive
 * construction as two distinct injected dependencies by orchestrating their
 * call sequence correctly to achieve initial construction and wiring.
 * 
 * @author jheinnic
 *
 * @param <Port>
 * @param <Driver>
 * @param <PortBuilder>
 */
public class PortDriverFactory<Port, Driver, PortBuilder> implements IPortDriverFactory<Port, Driver, PortBuilder> {
	private final IFactory<Port, PortBuilder> portFactory;
	private final IFactory<Driver, Consumer<Port>> driverFactory;
	private final Function<Port, Consumer<Driver>> injectionAdapter;

	public PortDriverFactory(IFactory<Port, PortBuilder> portBinder, IFactory<Driver, Consumer<Port>> driverFactory,
			Function<Port, Consumer<Driver>> injectionAdapter) {
		this.portFactory = portBinder;
		this.driverFactory = driverFactory;
		this.injectionAdapter = injectionAdapter;
	}

	public PortDriverFactory(Supplier<Port> portSupplierFunction, Function<Port, Driver> driverFactoryFunction,
			Function<Port, Consumer<Driver>> injectionAdapter)
	{
		this.portFactory = new SimplePortFactory<Port, PortBuilder>(portSupplierFunction);
		this.driverFactory = new SimpleDriverFactory<Port, Driver>(driverFactoryFunction);
		this.injectionAdapter = injectionAdapter;
	}

	@Override
	public final ImmutableList<? extends Driver> buildPortDrivers(Builder<Port> portListBuilder,
		IDirector<PortBuilder> portItemDirector)
	{
		final IFactory<ImmutableList<Port>, IImmutableListBuilder<PortBuilder>> listBuilderSupplier =
			new ImmutableListBuilderSupplier<Item, ItemBuilder>(this.portFactory);
		this.portFactory.apply(portItemDirector);
		final ImmutableList.Builder<Driver> driverListBuilder = ImmutableList.<Driver>builder();

		// portListBuilder.build().parallelStream().map(this.injectionAdapter)
		// .map( this.driverFactory.)
		// .forEach(driverListBuilder::add);

		return driverListBuilder.build();
	}
}
