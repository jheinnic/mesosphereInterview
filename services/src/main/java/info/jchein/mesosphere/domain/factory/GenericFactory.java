package info.jchein.mesosphere.domain.factory;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

@ValidateOnExecution(type = { ExecutableType.CONSTRUCTORS })
public class GenericFactory<BuilderImpl extends Supplier<Item> & ICastable<ItemBuilder>, ItemBuilder, Item> implements IBuilderFactory<ItemBuilder, Item> {
//public class GenericFactory<BuilderImpl extends ISupplierBuilder<ItemBuilder, Item>, ItemBuilder, Item> implements IBuilderFactory<ItemBuilder, Item> {
	private final Supplier<BuilderImpl> builderFactory;

	/**
	 * A default implementation is provided for the common case of ItemBuilder implementations that yield their output item
	 * by implementing both Supplier<Item> and ICastable<B> where B is the IDirector-exposed builder API.
	 * 
	 * @param builderFactory
	 */
	GenericFactory(@NotNull Supplier<BuilderImpl> builderFactory) {
		this.builderFactory = builderFactory;
	}

//	public GenericFactory(Supplier<ImmutableListSupplierBuilder<ItemBuilder, Item>> builderFactory2) {
//		 TODO Auto-generated constructor stub
//	}

	@Override
	public Item apply(Consumer<ItemBuilder> director) {
		final BuilderImpl builder = this.builderFactory.get();
		director.accept(builder.cast());
		return builder.get();
	}
}
