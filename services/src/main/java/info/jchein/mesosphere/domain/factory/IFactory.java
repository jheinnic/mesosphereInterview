package info.jchein.mesosphere.domain.factory;

import java.util.function.Function;

@FunctionalInterface
public interface IFactory<Item, ItemBuilder> extends Function<IDirector<ItemBuilder>, Item> 
{
	// Implied by @FunctionalInterface and Function inheritance
	// Item make(IDirector<ItemBuilder> builder);
}
