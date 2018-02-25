package info.jchein.mesosphere.elevator.control.manifest;

import java.util.function.Function;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;

public interface ITravelGraphFactory extends Function<DirectionOfTravel, ITravelGraph>
{
   
}
