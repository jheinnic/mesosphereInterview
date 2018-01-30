package info.jchein.mesosphere.elevator.runtime;

import rx.functions.Action1;

@FunctionalInterface
public interface IIntervalHandler extends Action1<Long> {

}
