package info.jchein.mesosphere.domain.factory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class ImmutableListFactory<Item, ItemBuilder> implements IFactory<ImmutableList<Item>, IImmutableListBuilder<ItemBuilder>>
{
	private IFactory<Item, ItemBuilder> itemFactory;

	public ImmutableListFactory(IFactory<Item, ItemBuilder> itemFactory) {
		this.itemFactory = itemFactory;
	}

	@Override
	public ImmutableList<Item> apply(IDirector<IImmutableListBuilder<ItemBuilder>> listDirector) {
		ImmutableListBuilderSupplier<Item, ItemBuilder> listBuilder = this.getListBuilder();
		listDirector.accept(listBuilder);
		return listBuilder.build();
	}
	
	// TODO: Suitable for Spring method injection replacement to avoid direct constructor call
	private ImmutableListBuilderSupplier<Item, ItemBuilder> getListBuilder() {
		return new ImmutableListBuilderSupplier<Item, ItemBuilder>(itemFactory);
	}
}
