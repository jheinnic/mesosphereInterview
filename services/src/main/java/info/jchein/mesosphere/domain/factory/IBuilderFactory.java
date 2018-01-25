package info.jchein.mesosphere.domain.factory;

import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface IBuilderFactory<ItemBuilder, Item> extends Function<Consumer<ItemBuilder>, Item> 
{
	// Implied by @FunctionalInterface and Function inheritance
	// Item make(IDirector<ItemBuilder> builder);
}
