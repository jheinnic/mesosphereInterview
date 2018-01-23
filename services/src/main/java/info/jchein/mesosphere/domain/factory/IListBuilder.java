package info.jchein.mesosphere.domain.factory;

public interface IListBuilder<ItemBuilder, ListBuilder extends IListBuilder<ItemBuilder, ListBuilder>> {
	ListBuilder add(IDirector<ItemBuilder> director);
}
