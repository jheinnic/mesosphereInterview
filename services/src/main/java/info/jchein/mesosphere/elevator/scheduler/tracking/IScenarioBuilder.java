package info.jchein.mesosphere.elevator.scheduler.tracking;

import info.jchein.mesosphere.domain.factory.IDirector;

public interface IScenarioBuilder {
	public IScenarioBuilder addElevatorCar(int carIndex, int floorIndex, IDirector<ICarDetailsBuilder> carDetails);
	
	public IScenarioBuilder schedulePickupCall(long timestamp, int floorIndex, )
}
