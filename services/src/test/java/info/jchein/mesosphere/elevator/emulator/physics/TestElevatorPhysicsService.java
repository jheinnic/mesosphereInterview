package info.jchein.mesosphere.elevator.emulator.physics;


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

import info.jchein.mesosphere.configuartion.tests.EnableTestVirtualRuntime;
import info.jchein.mesosphere.elevator.common.bootstrap.BuildingProperties;
import info.jchein.mesosphere.elevator.common.graph.ProtectedBitSet;
import info.jchein.mesosphere.elevator.common.graph.VertexFactory;

@RunWith(SpringRunner.class)
@EnableTestVirtualRuntime
public class TestElevatorPhysicsService
{
   public static final int BIT_SET_FLOOR_REQUEST = 9;

   @Autowired
   IElevatorPhysicsService sut;

   @Test
   public void whenReqUnitBitSet_thenCorrectBitIsSet()
   {
   }
}
