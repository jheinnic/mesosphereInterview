package info.jchein.mesosphere.elevator.monitor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.Subscribe;

import info.jchein.mesosphere.elevator.control.event.ElevatorCarEvent;
import info.jchein.mesosphere.elevator.control.event.LandingEvent;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeClock;
import info.jchein.mesosphere.elevator.simulator.event.TravellerEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ElevatorEventMonitor
{
   private IRuntimeEventBus eventBus;
   private IRuntimeClock clock;

   @Autowired
   public ElevatorEventMonitor(IRuntimeEventBus eventBus, IRuntimeClock clock)
   {
      this.eventBus = eventBus;
      this.clock = clock;
   }
   
   @PostConstruct
   public void init()
   {
      this.eventBus.registerListener(this);
   }
   
   @Subscribe
   public void elevatorCarEvent(ElevatorCarEvent event) {
      log.info("Observed elevator car event at {}: {}", this.clock.now(), event.toString());
   }
   
   @Subscribe
   public void landingEvent(LandingEvent event) {
      log.info("Observed landing floor event at {}: {}", this.clock.now(), event.toString());
   }
   
   @Subscribe
   public void travellerEvent(TravellerEvent event) {
      log.info("Observed simulated traveller event at {}: {}", this.clock.now(), event.toString());
   }
}
