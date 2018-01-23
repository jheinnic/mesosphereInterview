package info.jchein.mesosphere.elevator.domain.sdk;

public interface ICommonPort {
	void attachCommon(ICommonDriver driver);
	
	/**
	 * Common-driver-defined clock's current time.  Use this to enable virtualization during simulations
	 * instead of the Systent.currentTimeMillis() alternative.
	 * 
	 * @return
	 */
	long getCurrentTime();
	
	/**
	 * Control method exposed for subtypes of attached driver to scheduled future timed events.
	 * Implementation is delegated to attached CommonDriver.
	 * 
	 * @param key
	 * @param delayMs
	 */
    void setTimer(String key, long delayMs);

	/**
	 * Control method exposed for subtypes of attached driver to attempt cancelation of a previously
	 * scheduled timer by its given key.
	 * Implementation is delegated to attached CommonDriver.
	 * 
	 * @param key
	 * @param delayMs
	 */
    void abortTimer(String key);

    /**
     * Effector method exposed for the CommonDriver to signal that its alarm mechanism has fired,
     * regardless of what that mechanism is.
     * 
     * Unlike the set and abort methods, where the common port delegates calls to the common driver,
     * the fire and cancel methods are delegated to the abstract port during the course of the common
     * drivers timer implementation.  Common port does not implement these methods, they are left as
     * extension points.  Concrete port types are responsible for defining the mechanism by which
     * alarm timers and/or cancellation acknowledgments are delivered to their bound drivers.
     * 
     * @param key
     */
    void fireTimer(String key);

    /**
     * Effector method exposed for the CommonDriver to signal confirmation that it was able to cancel
     * an alarm as requested by prior call to {@link #abortTimer(String)}.
     * 
     * Unlike the set and abort methods, where the common port delegates calls to the common driver,
     * the fire and cancel methods are delegated to the common port during the course of the common
     * drivers timer implementation.  Common port does not implement these methods, they are left as
     * extension points.  Concrete port types are responsible for defining the mechanism by which
     * alarm timers and/or cancellation acknowledgments are delivered to their bound drivers.
     * 
     * @param key
     */
    void cancelTimer(String key);
}
