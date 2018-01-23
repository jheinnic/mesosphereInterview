package info.jchein.mesosphere.domain.subsystem;

import java.util.function.Consumer;

import info.jchein.mesosphere.domain.factory.IFactory;

public interface IDriverFactory<Port, Driver> extends IFactory<Driver, Consumer<Port>> 
{
	// No need to specify--its defined by IFactory.
	// Driver make(IDirector<IBinder<Port>> director);
}
