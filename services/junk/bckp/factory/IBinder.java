package info.jchein.mesosphere.domain.factory;

import java.util.function.Consumer;

@FunctionalInterface
public interface IBinder<T> extends Consumer<T> {
	// void accept( T dependency );
}
