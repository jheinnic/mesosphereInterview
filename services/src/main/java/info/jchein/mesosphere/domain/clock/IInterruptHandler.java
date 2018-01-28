package info.jchein.mesosphere.domain.clock;

import rx.functions.Action1;

@FunctionalInterface
public interface IInterruptHandler extends Action1<Long> {

}
