package info.jchein.mesosphere.elevator.runtime;

@FunctionalInterface
public interface IVariableIntervalFunction {
   public long apply(long interval);
}
