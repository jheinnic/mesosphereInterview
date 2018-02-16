package info.jchein.mesosphere.domain.factory;

import java.util.function.Supplier;

public interface ISupplierBuilder<ItemBuilder, Item> extends Supplier<Item>, ICastable<ItemBuilder> {

}
