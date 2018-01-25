package info.jchein.mesosphere.elevator.emulator;

import java.util.concurrent.TimeUnit;

public interface IEmulatedPassengerStop {
	public void blockDoorClosing(long duration, TimeUnit timeUnit);
	public void boardPassengers(int actualGroupSize, int estimatedCount, double cumulativeWeight );
	public void disembarkPassengers(int actualGroupSize, int estimatedCount, double cumulativeWeight );

}
