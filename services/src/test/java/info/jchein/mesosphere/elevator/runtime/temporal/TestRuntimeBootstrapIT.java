package info.jchein.mesosphere.elevator.runtime.temporal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.temporal.temporal.IVirtualRuntimeService;
import info.jchein.mesosphere.test.config.runtime.temporal.EnableTestVirtualRuntime;

@EnableTestVirtualRuntime
@RunWith(SpringJUnit4ClassRunner.class)
public class TestRuntimeBootstrapIT
{
   @Autowired
   IRuntimeClock clock;
   
   @Autowired
   IRuntimeEventBus eventBus;
   
   @Autowired
   IRuntimeScheduler scheduler;
   
   @Test
   public void whenBootstrap_thenInjectBus() { 
      assertThat(this.eventBus).isNotNull();
   }
   
   @Test
   public void whenBootstrap_thenInjectClock() { 
      assertThat(this.clock).isNotNull();
   }
   
   @Test
   public void whenBootstrap_thenInjectScheduler() { 
      assertThat(this.scheduler).isNotNull();
   }
}
