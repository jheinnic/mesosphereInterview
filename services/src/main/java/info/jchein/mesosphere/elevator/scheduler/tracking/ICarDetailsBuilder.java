package info.jchein.mesosphere.elevator.scheduler.tracking;

import info.jchein.mesosphere.domain.factory.IDirector;

public interface ICarDetailsBuilder {
	public ICarDetailsBuilder moving( int destination, boolean forPickup, boolean forDropOff, double eta );
	
	public ICarDetailsBuilder passengerCommitment( double totalWeight, IDirector<ManifestBuilder> origins );
}
