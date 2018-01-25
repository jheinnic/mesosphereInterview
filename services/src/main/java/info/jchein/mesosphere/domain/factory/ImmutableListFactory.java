package info.jchein.mesosphere.domain.factory;

import com.google.common.collect.ImmutableList;

public class ImmutableListFactory<ItemBuilder, Item> extends GenericFactory<ImmutableListSupplierBuilder<ItemBuilder, Item>, IListBuilder<ItemBuilder>, ImmutableList<Item>>
{
	public ImmutableListFactory(IBuilderFactory<ItemBuilder, Item> itemFactory) {
		super( () -> new ImmutableListSupplierBuilder<ItemBuilder, Item>(itemFactory) );
	}
}
