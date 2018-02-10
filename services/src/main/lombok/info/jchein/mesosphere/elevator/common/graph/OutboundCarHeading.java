package info.jchein.mesosphere.elevator.common.graph;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.validator.annotation.Moving;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(doNotUseGetters=true)
public class OutboundCarHeading
{
   private final int floorIndex;
   private final int carIndex;
   
   @Moving
   private final DirectionOfTravel direction;
}
