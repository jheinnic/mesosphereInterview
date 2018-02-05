package info.jchein.mesosphere.elevator.runtime.virtual;


import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class VirtualRuntimeService implements IVirtualRuntimeService
{
   @Getter
   @Delegate
   private final IRuntimeClock clock;
   
   @Getter
   @Delegate
   private final IRuntimeEventBus eventBus;
   
   @Getter
   @Delegate
   private final IRuntimeScheduler scheduler;

   boolean hasBegun = false;
   
   @Autowired
   VirtualRuntimeService(@NotNull IRuntimeClock clock, @NotNull IRuntimeEventBus eventBus, @NotNull VirtualScheduler scheduler) {
      this.clock = clock;
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