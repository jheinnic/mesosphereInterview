package info.jchein.mesosphere.elevator.common.graph;

import javax.validation.constraints.NotNull;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Graph node represening a specific car either stopped or travelling through a specifc floor.
 * 
 * @author jheinnic
 */
@Value
@EqualsAndHashCode(doNotUseGetters=true)
public class CarTravelType
{
   private final int floorIndex;
   private final int carIndex;

   @NotNull
   private final DirectionOfTravel direction;
}
