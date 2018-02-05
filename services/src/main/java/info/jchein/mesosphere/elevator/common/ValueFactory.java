package info.jchein.mesosphere.elevator.common;

import java.util.UUID;

public class ValueFactory implements IValueFactory
{
   @Override
   public IPassengerId getNextPassengerId()
   {
      return new PassengerId(
         UUID.randomUUID());
   }
}
