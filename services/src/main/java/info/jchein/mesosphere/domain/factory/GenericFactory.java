package info.jchein.mesosphere.domain.factory;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

import info.jchein.mesosphere.domain.factory.IBinder;
import info.jchein.mesosphere.domain.factory.IBuilder;
import info.jchein.mesosphere.domain.factory.IDirector;
import info.jchein.mesosphere.domain.factory.IFactory;

@ValidateOnExecution(type = { ExecutableType.CONSTRUCTORS })
public class GenericFactory<ItemBuilder, Item> implements IFactory<Item, ItemBuilder> {
	private final Supplier<ItemBuilder> builderFactory;
	private final Function<ItemBuilder, Item> postBuildFunction;

	GenericFactory(@NotNull Supplier<ItemBuilder> builderFactory, @NotNull Function<ItemBuilder, Item> postBuildFunction) {
		this.builderFactory = builderFactory;
		this.postBuildFunction = postBuildFunction;
	}

	@SuppressWarnings("unchecked")
	/**
	 * A default implementation is provided for the common case of ItemBuilder implementations that yield their output item
	 * by also implementing Supplier<Item>, in which case the transformation function from ItemBuilder to Supplier<Item> is
	 * a simple typecast operation.  The compiler only knows that the implementation implements ItemBuilder, so we must
	 * disable 
	 * 
	 * @param builderFactory
	 */
	GenericFactory(@NotNull Supplier<ItemBuilder> builderFactory) {
		this(builderFactory, builder -> {
			Supplier<Item> castBuilder = (Supplier<Item>) builder;
			return castBuilder.get();
		});
	}

	@Override
	public Item apply(IDirector<ItemBuilder> director) {
		ItemBuilder builder = this.builderFactory.get();
		director.accept(builder);
		return this.postBuildFunction.apply(builder);
	}
}
