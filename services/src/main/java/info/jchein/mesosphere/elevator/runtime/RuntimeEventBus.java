package info.jchein.mesosphere.elevator.runtime;

import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;

@Component
@ValidateOnExecution(type= {ExecutableType.ALL})
public class RuntimeEventBus implements IRuntimeEventBus 
{
   private final EventBus eventBus;
 
   @Autowired
   public RuntimeEventBus(@NotNull EventBus eventBus) {
      this.eventBus = eventBus;
   }

   @Override
   public void post(@NotNull Object event)
   {
      this.eventBus.post(event);
   }

   @Override
   public void registerListener(@NotNull Object listener)
   {
      this.eventBus.register(listener);
   }

   @Override
   public void unregisterListener(@NotNull Object listener)
   {
      this.eventBus.unregister(listener);
   }
}
