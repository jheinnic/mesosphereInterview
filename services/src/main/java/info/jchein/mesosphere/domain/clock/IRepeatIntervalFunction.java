package info.jchein.mesosphere.domain.clock;

import java.util.function.Function;

@FunctionalInterface
public interface IRepeatIntervalFunction extends Function<Long, Long> {

}
