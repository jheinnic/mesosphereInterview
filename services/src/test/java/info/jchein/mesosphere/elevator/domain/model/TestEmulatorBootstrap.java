package info.jchein.mesosphere.elevator.domain.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.IRuntimeService;
import info.jchein.mesosphere.elevator.runtime.RuntimeFrameworkConfiguration;

@TestPropertySource
@ContextConfiguration(classes= {RuntimeFrameworkConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class TestEmulatorBootstrap
{
   @Autowired
   IRuntimeClock cloc;
   
   @Autowired
   IRuntimeEventBus eventBus;
   
   @Autowired
   IRuntimeScheduler scheduler;
   
   @Autowired
   IRuntimeService service;
   
   @Test
   public void test() { }
}
