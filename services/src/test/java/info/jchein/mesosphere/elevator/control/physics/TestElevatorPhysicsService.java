package info.jchein.mesosphere.elevator.control.physics;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import info.jchein.mesosphere.elevator.common.Assertions;
import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentProperties;
import info.jchein.mesosphere.elevator.common.graph.ProtectedBitSet;
import info.jchein.mesosphere.elevator.common.graph.VertexFactory;
import info.jchein.mesosphere.elevator.common.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.common.physics.JourneyArc;
import info.jchein.mesosphere.test.config.runtime.temporal.EnableTestVirtualRuntime;

@RunWith(SpringRunner.class)
@EnableTestVirtualRuntime
public class TestElevatorPhysicsService
{
   public static final int BIT_SET_FLOOR_REQUEST = 9;
   
   public static final double TICK_DURATION_A = 0.125;

   @Autowired
   IElevatorPhysicsService sut;

   @Test
   public void whenQueryIdealPassengerCount_thenResultFromConfig()
   {
//      Assertions.assertThat(sut).hasIdealPassengerCount(10);
   }
   
   @Test
   public void whenOneLevelUpTickA_thenFoo() {
      JourneyArc arc = this.sut.getTraversalPath(1, 2);
      
//      Assertions.assertThat(arc.asMomentIterable(TICK_DURATION_A));
      
   }
}
