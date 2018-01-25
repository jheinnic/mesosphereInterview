package info.jchein.mesosphere.domain.subsystem;

import java.util.function.Consumer;
import java.util.function.Function;

import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

import info.jchein.mesosphere.domain.factory.IBinder;
import info.jchein.mesosphere.domain.factory.IBuilder;
import info.jchein.mesosphere.domain.factory.IDirector;
import info.jchein.mesosphere.domain.factory.IBuilderFactory;

@ValidateOnExecution(type= {ExecutableType.CONSTRUCTORS})
public class SimpleDriverFactory<Port, Driver> implements IBuilderFactory<Driver, Consumer<Port>>
{
	private final Function<Port, Driver> factoryFunction;
	SimpleDriverFactory(@NotNull Function<Port, Driver> factoryFunction) {
		this.factoryFunction = factoryFunction;
	}

	@Override
	public Driver apply(IDirector<Consumer<Port>> t) {
		SimpleDriverBuilder builder = new SimpleDriverBuilder();
		t.accept(builder);
		return builder.build();
	}
	
	@ValidateOnExecution(type= {ExecutableType.NON_GETTER_METHODS})
	private class SimpleDriverBuilder implements Consumer<Port>, IBuilder<Driver> {
		Port input;

		@Override
		public void accept(@NotNull Port t) {
			this.input = t;
		}
		
		public Driver build() {
			return SimpleDriverFactory.this.factoryFunction.apply(this.input);
		}
	}
}
