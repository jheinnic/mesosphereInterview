package info.jchein.mesosphere.elevator.control;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.Subscribe;

import info.jchein.mesosphere.elevator.control.event.ElevatorCarEvent;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBusLocator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ElevatorScopedEventEcho
{
   private final IRuntimeEventBus mainEventBus;
   private final IElevatorCarScope elevatorCarScope;
   private final IRuntimeEventBusLocator locator;

   @Autowired
   ElevatorScopedEventEcho(IRuntimeEventBus mainEventBus, @NotNull IElevatorCarScope elevatorCarScope, @NotNull IRuntimeEventBusLocator locator) {
      this.mainEventBus = mainEventBus;
      this.elevatorCarScope = elevatorCarScope;
      this.locator = locator;
   }
   
   @PostConstruct
   public void init() {
      log.info("ElevatorScopedEventEcho init!");
      this.mainEventBus.registerListener(this);
   }
   
   @Subscribe
   public void onMessage(ElevatorCarEvent event) {
      log.info("ElevatorScopedEventEcho handled a message in green?");
      final int carIndex = event.getCarIndex();
      this.elevatorCarScope.evalForCar(carIndex, () -> {
         return locator.locate(IElevatorCarScope.LOCAL_EVENT_BUS_NAME);
      }).post(event);
   }
}
