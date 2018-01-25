package info.jchein.mesosphere.domain.subsystem;

import java.util.function.Consumer;

import info.jchein.mesosphere.domain.factory.IBuilderFactory;

public interface IDriverFactory<Port, Driver> extends IBuilderFactory<Driver, Consumer<Port>> 
{
	// No need to specify--its defined by IFactory.
	// Driver make(IDirector<IBinder<Port>> director);
}
