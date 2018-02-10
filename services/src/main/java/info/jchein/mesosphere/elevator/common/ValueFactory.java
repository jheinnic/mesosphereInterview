package info.jchein.mesosphere.elevator.common;

import java.util.UUID;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ValueFactory implements IValueFactory
{
   @Override
   public PassengerId getNextPassengerId()
   {
      return new PassengerId(
         UUID.randomUUID());
   }
}
