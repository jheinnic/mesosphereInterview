package info.jchein.mesosphere.elevator.runtime.virtual;


import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class VirtualRuntimeService implements IVirtualRuntimeService
{
   private final IRuntimeEventBus eventBus;
   
   private final IRuntimeScheduler scheduler;

   boolean hasBegun = false;
   
   @Autowired
   VirtualRuntimeService(@NotNull IRuntimeEventBus eventBus, @NotNull VirtualScheduler scheduler) {
      this.eventBus = eventBus;
      this.scheduler = scheduler;
   }
   
   @PostConstruct
   void init() {
      this.eventBus.registerListener(this.scheduler);
   }

   @Override
   public synchronized void begin()
   {
      Preconditions.checkState(this.hasBegun == false, "May only begin the runtime once");

      this.eventBus.post(BeginRuntimeEvent.INSTANCE);
      this.eventBus.unregisterListener(this.scheduler);

      this.hasBegun = true;
      log.info("Runtime service begin method completed");
   }
}
