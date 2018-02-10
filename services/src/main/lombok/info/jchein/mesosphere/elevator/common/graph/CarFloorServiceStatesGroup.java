package info.jchein.mesosphere.elevator.common.graph;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class CarFloorServiceStatesGroup
{
   int carIndex;
   int floorIndex;

   CarServiceState upwardBraking;
   CarServiceState upwardBoarding;
   CarServiceState upwardTravelling;
   
   CarServiceState downwardBraking;
   CarServiceState downwardBoarding;
   CarServiceState downwardTravelling;

   CarServiceState parked;
}
