package info.jchein.mesosphere.domain.subsystem;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.domain.factory.IFactory;
import info.jchein.mesosphere.domain.factory.IImmutableListBuilder;

/**
 * It is important to use implementation classes with type arguments from this
 * package. This is what facilitates providing public methods for attachment in
 * the implementations without polluting the runtime API with them as well.
 * 
 * In the case of this Port factory, be sure to use the implementation type of
 * your Port class, not its public API.
 * 
 * @author jheinnic
 *
 * @param <Port>
 * @param <Driver>
 */
public interface IPortFactory<Port, PortBuilder> extends IFactory<Port, PortBuilder> {
	// Implied by functional inheritance
	// ImmutableList<Port> apply(IDirector<IImmutableListBuilder<PortBuilder>> director);
}

// void createPorts(ImmutableList.Builder<? super Port> listBuilder,
// IDirector<PortListBuilder> director);
// void createPorts(ImmutableList.Builder<IBinder<Driver>> listBuilder,
// IDirector<PortBuilder> director);
