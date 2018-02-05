package info.jchein.mesosphere.elevator.runtime;

@FunctionalInterface
public interface IIntervalHandler { // extends Action1<Long> {
    public void call(long interval);
}
