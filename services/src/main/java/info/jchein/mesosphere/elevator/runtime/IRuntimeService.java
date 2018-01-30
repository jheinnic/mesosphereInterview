package info.jchein.mesosphere.elevator.runtime;

public interface IRuntimeService extends IRuntimeClock, IRuntimeScheduler, IRuntimeEventBus {
	IRuntimeClock getClock();
	
	IRuntimeScheduler getScheduler();
	
	IRuntimeEventBus getEventBus();
	
	void begin();
}
