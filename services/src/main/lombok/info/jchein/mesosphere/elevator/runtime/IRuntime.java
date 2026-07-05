package info.jchein.mesosphere.elevator.runtime;

/**
 * A static utility class for the elevator runtime concerns.
 * 
 * @author jheinnic
 */
public enum IRuntime {
   PHYSICAL,
   VIRTUAL
   ;

   /**
    * A Qualifier string used to tag any beans created for the runtime from a third party library, in order to
    * mitigate potential conflicts with other compoments that may wish to reuse instances of the same library as
    * beans for their own purpose.
    */
	public static final String ELEVATOR_RUNTIME_QUALIFIER = "mesosphere.elevator.control.runtime";
}
