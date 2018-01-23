package info.jchein.mesosphere.domain.factory;

import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

@ValidateOnExecution(type= {ExecutableType.CONSTRUCTORS, ExecutableType.NON_GETTER_METHODS})
public final class ImmutableListBuilderSupplier<Item, ItemBuilder> implements IImmutableListBuilder<ItemBuilder>, IBuilder<ImmutableList<Item>>
{
	private final Builder<Item> listBuilder;
	private final IFactory<Item, ItemBuilder> itemFactory;

	public ImmutableListBuilderSupplier(@NotNull IFactory<Item, ItemBuilder> itemFactory) {
		this(itemFactory, ImmutableList.<Item>builder());
	}

	public ImmutableListBuilderSupplier(@NotNull IFactory<Item, ItemBuilder> itemFactory, @NotNull Builder<Item> listBuilder) {
		this.itemFactory = itemFactory;
		this.listBuilder = listBuilder;
	}

	@Override
	public IImmutableListBuilder<ItemBuilder> add(@NotNull IDirector<ItemBuilder> itemDirector) {
		this.listBuilder.add(
			this.itemFactory.apply(itemDirector));
		return this;
	}

	@Override
	public ImmutableList<Item> build() {
		return this.listBuilder.build();
	}

}
