package info.jchein.mesosphere.elevator.common;

import javax.validation.constraints.Min;

import lombok.Value;

@Value(staticConstructor="of")
public class CarIndex
{
   @Min(0)
   final int carIndex;
}
