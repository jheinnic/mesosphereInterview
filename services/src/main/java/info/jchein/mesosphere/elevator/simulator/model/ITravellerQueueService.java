package info.jchein.mesosphere.elevator.simulator.model;

public interface ITravellerQueueService {
    void queueForPickup(ISimulatedTraveller pickupRequest);
}
