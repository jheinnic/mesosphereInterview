package info.jchein.mesosphere.elevator.emulator.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import fixtures.elevator.runtime.temporal.VirtualRuntimeTestConfiguration;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.temporal.VirtualTimerConfiguration;
import info.jchein.mesosphere.elevator.runtime.temporal.temporal.EnableVirtualRuntime;
import info.jchein.mesosphere.elevator.runtime.temporal.temporal.IVirtualRuntimeService;

@TestPropertySource
@ContextConfiguration(classes= {VirtualRuntimeTestConfiguration.class}, loader=AnnotationConfigContextLoader.class)
@ActiveProfiles("elevator.runtime.virtual")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestEmulatorBootstrapIT
{
   @Autowired
   IRuntimeClock clock;
   
   @Autowired
   IRuntimeEventBus eventBus;
   
   @Autowired
   IRuntimeScheduler scheduler;
   
   @Autowired
   IVirtualRuntimeService service;
   
   @Test
   public void whenBootstrap_thenInjectClock() { 
      assertThat(this.eventBus).isNotNull();
   }
   
   @Test
   public void whenBootstrap_thenInjectBus() { 
      assertThat(this.clock).isNotNull();
   }
   
   @Test
   public void whenBootstrap_thenInjectScheduler() { 
      assertThat(this.scheduler).isNotNull();
   }
   
   @Test
   public void whenBootstrap_thenInjectService() { 
      assertThat(this.service).isNotNull();
   }
}
