package info.jchein.mesosphere.domain.subsystem;

import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

import com.google.common.base.Supplier;

import info.jchein.mesosphere.domain.factory.IDirector;
import info.jchein.mesosphere.domain.factory.IFactory;

@ValidateOnExecution(type= {ExecutableType.CONSTRUCTORS})
public class SimplePortFactory<Port, PortBuilder> implements IFactory<Port, PortBuilder>
{
	private final Supplier<Port> supplierFunction;

	SimplePortFactory(@NotNull Supplier<Port> supplierFunction) {
		this.supplierFunction = supplierFunction;
	}

	@Override
	public Port apply(IDirector<? super PortBuilder> t) {
		Object foo;
		t.accept(foo);
		return this.supplierFunction.get();
	}
}
