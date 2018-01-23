package info.jchein.mesosphere.elevator.simulator.passengers;

public interface IPassengerArrivalStrategy {
    void passengerArrival(long timeIndex, int originFloorIndex, int destinationFloorIndex);
}
