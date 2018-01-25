package info.jchein.mesosphere.domain.factory;

import java.util.function.Consumer;

public interface IBuilderWithList<ItemBuilder, BuilderWithList extends IBuilderWithList<ItemBuilder, BuilderWithList>> {
	BuilderWithList add(Consumer<ItemBuilder> director);
}
