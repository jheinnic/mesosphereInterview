package info.jchein.mesosphere.domain.subsystem;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import info.jchein.mesosphere.domain.factory.IBuilderFactory;
import info.jchein.mesosphere.domain.factory.IListBuilder;
import info.jchein.mesosphere.domain.factory.ISupplierBuilder;
import info.jchein.mesosphere.domain.factory.ImmutableListSupplierBuilder;

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
	private final IBuilderFactory<PortBuilder, Port> portFactory;
	private final IBuilderFactory<Consumer<Port>, Driver> driverFactory;
	private final Function<Port, Consumer<Driver>> injectionAdapter;

	public PortDriverFactory(IBuilderFactory<PortBuilder, Port> portBinder, IBuilderFactory<Consumer<Port>, Driver> driverFactory,
			Function<Port, Consumer<Driver>> injectionAdapter) {
		this.portFactory = portBinder;
		this.driverFactory = driverFactory;
		this.injectionAdapter = injectionAdapter;
	}

//	public PortDriverFactory(Supplier<Port> portSupplierFunction, Function<Port, Driver> driverFactoryFunction,
//			Function<Port, Consumer<Driver>> injectionAdapter)
//	{
//		this.portFactory = new SimplePortFactory<PortBuilder, Port>(portSupplierFunction);
//		this.driverFactory = new SimpleDriverFactory<Port, Driver>(driverFactoryFunction);
//		this.injectionAdapter = injectionAdapter;
//	}

	@Override
	public final ImmutableList<? extends Driver> buildPortDrivers(Builder<Port> portListBuilder,
		Consumer<IListBuilder<PortBuilder>> portItemDirector)
	{
		final ImmutableListSupplierBuilder<PortBuilder, Port> listBuilderSupplier =
			new ImmutableListSupplierBuilder<PortBuilder, Port>(this.portFactory);
		portItemDirector.accept(listBuilderSupplier.cast());
		final ImmutableList.Builder<Driver> driverListBuilder = ImmutableList.<Driver>builder();

//		 portListBuilder.build().parallelStream().map(this.injectionAdapter)
//		 .map( this.driverFactory::accept )
//		 .forEach(driverListBuilder::add);

		return driverListBuilder.build();
	}
}
