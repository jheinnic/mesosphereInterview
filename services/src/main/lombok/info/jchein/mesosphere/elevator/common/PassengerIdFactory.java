package info.jchein.mesosphere.elevator.common;

import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class PassengerIdFactory implements Supplier<PassengerId>
{
   @Override
   public PassengerId get()
   {
      return new PassengerId(
         UUID.randomUUID());
   }
}
