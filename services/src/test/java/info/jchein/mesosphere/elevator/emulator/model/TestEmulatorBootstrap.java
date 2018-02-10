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

import info.jchein.mesosphere.configuartion.tests.VirtualRuntimeTestConfiguration;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.virtual.EnableVirtualRuntime;
import info.jchein.mesosphere.elevator.runtime.virtual.IVirtualRuntimeService;
import info.jchein.mesosphere.elevator.runtime.virtual.VirtualRuntimeConfiguration;

@TestPropertySource
@ContextConfiguration(classes= {VirtualRuntimeTestConfiguration.class}, loader=AnnotationConfigContextLoader.class)
@ActiveProfiles("elevator.runtime.virtual")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestEmulatorBootstrap
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
