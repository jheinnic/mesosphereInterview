package info.jchein.mesosphere.elevator.control;

import java.util.function.Supplier;

import org.xml.sax.Locator;

public interface IElevatorCarScope
{
   public static final String SCOPE_NAME = "mesosphere.elevator.car";
   public static final String LOCAL_EVENT_BUS_NAME = "carLocalRuntimeEventBus";
   /**
    * Car index context invoker.
    * 
    * @param carIndex The index slot number that will be contextually active when {@link director#apply(Locator)} is called.
    * @param director A director procedure to be executed in scope of the given {@code carIndex}.  Its return value becomes
    *        the return value from this function.
    * @return Whatever director returns.
    */
   <R> R evalForCar(int carIndex, Supplier<R> director);
}
