package info.jchein.mesosphere.elevator.control.model;

import java.util.function.Consumer;
import java.util.function.Function;

public interface IElevatorCarScope
{
   public static final String SCOPE_NAME = "mesosphere.elevator.car";
   /**
    * Car index context invoker.
    * 
    * @param carIndex The index slot number that will be contextually active when {@link director#apply(Locator)} is called.
    * @param locator A service locator interface that will be passed as argument to the director function once the contextual
    *        car index has been set.
    * @param director A director procedure to be executed in scope of the given {@code carIndex}.  Its return value becomes
    *        the return value from this function.
    * @return Whatever director returns.
    */
   <Locator> void evalForCar(int carIndex, Locator locator, Consumer<Locator> director);
}
