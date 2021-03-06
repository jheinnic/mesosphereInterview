package info.jchein.mesosphere.elevator.control.model;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import fixtures.elevator.emulator.model.EmulatorBootstrapTestConfiguration;
import fixtures.elevator.emulator.model.EnableTestEmulatedElevator;
import info.jchein.mesosphere.elevator.common.bootstrap.VirtualRuntimeProperties;
import info.jchein.mesosphere.elevator.control.ElevatorGroupControl;
import info.jchein.mesosphere.elevator.control.event.ElevatorCarEvent;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.temporal.temporal.RuntimeClock;
import rx.Observable;

@Import(EmulatorBootstrapTestConfiguration.class)
//@RunWith(SpringRunner.class)
public class TestElevatorControlGroup
{
   @ClassRule
   public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

   @Rule
   public final SpringMethodRule springMethodRule = new SpringMethodRule();
   
   @Configuration
   @Import(ElevatorGroupControl.class)
   public static class TestElevatorGroupControlConfiguration
   {

   }

   @MockBean
   Observable<ElevatorCarEvent> changeStream;
   
   @SpyBean
   IRuntimeEventBus eventBus;
   
   @Autowired
   ElevatorGroupControl sut;
   
   @Test
   public void runBlank() {
      int foo = 5;
      System.out.println("6");
   }
   
   @Test
   public void whenGetChangeStreamCalled_thenReturnObservableFromEventBus() {
      when(eventBus.<ElevatorCarEvent>toObservable()).thenReturn(changeStream);
      
      assertThat(sut.getChangeStream()).as("Observable change stream").isSameAs(changeStream);
   }
}
