package info.jchein.mesosphere.elevator.simulator.traveller;

public interface IProbabilityResolver<T> {
	T resolve(double randomValue);
}
