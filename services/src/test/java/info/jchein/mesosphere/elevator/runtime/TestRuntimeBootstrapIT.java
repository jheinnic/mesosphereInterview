package info.jchein.mesosphere.elevator.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import info.jchein.mesosphere.configuartion.tests.EnableTestVirtualRuntime;
import info.jchein.mesosphere.elevator.runtime.virtual.IVirtualRuntimeService;

@EnableTestVirtualRuntime
//@TestPropertySource
//@ContextConfiguration(classes= {VirtualRuntimeTestConfiguration.class}, loader=AnnotationConfigContextLoader.class)
//@ActiveProfiles("elevator.runtime.virtual")
@RunWith(SpringJUnit4ClassRunner.class)
public class TestRuntimeBootstrapIT
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
