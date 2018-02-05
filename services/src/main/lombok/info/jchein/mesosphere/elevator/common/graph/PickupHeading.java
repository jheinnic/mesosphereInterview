package info.jchein.mesosphere.elevator.common.graph;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(doNotUseGetters=true)
public class PickupHeading
{
   private int floorIndex;
   private DirectionOfTravel direction;
}
