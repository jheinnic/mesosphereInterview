package info.jchein.mesosphere.elevator.runtime.virtual;

public interface IVirtualRuntimeService {
	public static final String ELEVATOR_RUNTIME_QUALIFIER = "mesosphere.elevator.control.runtime";

    // IRuntimeClock getClock();
	
	// IRuntimeScheduler getScheduler();
	
	// IRuntimeEventBus getEventBus();
	
	void begin();
}
