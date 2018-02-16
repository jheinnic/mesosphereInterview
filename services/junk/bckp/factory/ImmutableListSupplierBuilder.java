package info.jchein.mesosphere.domain.factory;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

@ValidateOnExecution(type= {ExecutableType.CONSTRUCTORS, ExecutableType.NON_GETTER_METHODS})
public final class ImmutableListSupplierBuilder<ItemBuilder, Item> implements IListBuilder<ItemBuilder>, ICastable<IListBuilder<ItemBuilder>>, Supplier<ImmutableList<Item>>
{
	private final Builder<Item> listBuilder;
	private final IBuilderFactory<ItemBuilder, Item> itemFactory;

	public ImmutableListSupplierBuilder(@NotNull IBuilderFactory<ItemBuilder, Item> itemFactory) {
		this(itemFactory, ImmutableList.<Item>builder());
	}

	public ImmutableListSupplierBuilder(@NotNull IBuilderFactory<ItemBuilder, Item> itemFactory, @NotNull Builder<Item> listBuilder) {
		this.itemFactory = itemFactory;
		this.listBuilder = listBuilder;
	}

	@Override
	public IListBuilder<ItemBuilder> add(@NotNull Consumer<ItemBuilder> itemDirector) {
		this.listBuilder.add(
			this.itemFactory.apply(itemDirector));
		return this;
	}

	@Override
	public IListBuilder<ItemBuilder> cast() {
		return this;
	}

	@Override
	public ImmutableList<Item> get() {
		return this.listBuilder.build();
	}

}
