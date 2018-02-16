package info.jchein.mesosphere.domain.subsystem;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

import info.jchein.mesosphere.domain.factory.IBuilderFactory;

@ValidateOnExecution(type= {ExecutableType.CONSTRUCTORS})
public class SimpleDriverFactory<Port, Driver> implements IBuilderFactory<Consumer<Port>, Driver>
{
	private final Function<Port, Driver> factoryFunction;
	SimpleDriverFactory(@NotNull Function<Port, Driver> factoryFunction) {
		this.factoryFunction = factoryFunction;
	}

	@Override
	public Driver apply(Consumer<Consumer<Port>> t) {
		SimpleDriverBuilder builder = new SimpleDriverBuilder();
		t.accept(builder);
		return builder.get();
	}
	
	@ValidateOnExecution(type= {ExecutableType.NON_GETTER_METHODS})
	private class SimpleDriverBuilder implements Consumer<Port>, Supplier<Driver> {
		Port input;

		@Override
		public void accept(@NotNull Port t) {
			this.input = t;
		}
		
		public Driver get() {
			return SimpleDriverFactory.this.factoryFunction.apply(this.input);
		}
	}
}
