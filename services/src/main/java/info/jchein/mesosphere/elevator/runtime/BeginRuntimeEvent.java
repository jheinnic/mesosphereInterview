package info.jchein.mesosphere.elevator.runtime;

/**
 * Marker type for an event used by the RuntimeService to signal the begin() has been called to its
 * dependent components without needing to either bind their implementation types or leak begin() into
 * their delegation interfaces.
 * 
 * Only used internally, so no public declarations.
 * 
 * @author jheinnic
 */
class BeginRuntimeEvent
{
   private BeginRuntimeEvent() { }
   
   static final BeginRuntimeEvent INSTANCE = new BeginRuntimeEvent();
}
