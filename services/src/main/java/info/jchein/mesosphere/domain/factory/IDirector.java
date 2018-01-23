package info.jchein.mesosphere.domain.factory;

import java.util.function.Consumer;

@FunctionalInterface
public interface IDirector<B> extends Consumer<B> {
	// void accept(B builder);
}
