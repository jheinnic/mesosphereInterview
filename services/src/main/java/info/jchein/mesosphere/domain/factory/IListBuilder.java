package info.jchein.mesosphere.domain.factory;

import java.util.function.Consumer;

public interface IListBuilder<ItemBuilder> extends IBuilderWithList<ItemBuilder, IListBuilder<ItemBuilder>> {
	IListBuilder<ItemBuilder> add(Consumer<ItemBuilder> director);
}
