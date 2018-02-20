package info.jchein.mesosphere.elevator.common.graph;


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

import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentProperties;
import info.jchein.mesosphere.elevator.common.graph.ProtectedBitSet;
import info.jchein.mesosphere.elevator.common.graph.VertexFactory;
import info.jchein.mesosphere.test.config.runtime.temporal.EnableTestVirtualRuntime;

@RunWith(SpringRunner.class)
@EnableTestVirtualRuntime
public class TestVertexFactory
{
   public static final int BIT_SET_FLOOR_REQUEST = 9;

   @Autowired
   VertexFactory sut;

   Condition<ProtectedBitSet> bitSet = new Condition<ProtectedBitSet>("floor bit set") {
      @Override
      public boolean matches(ProtectedBitSet pbs) {
         return pbs.get(BIT_SET_FLOOR_REQUEST);
      }
   };

   @Test
   public void whenReqUnitBitSet_thenCorrectBitIsSet()
   {
      assertThat(
         sut.getSingleFloorBitSet(BIT_SET_FLOOR_REQUEST)
      ).as("Single floor bitset for floor %d", BIT_SET_FLOOR_REQUEST);
      
   }

   @Test
   public void whenConfigMillisPerTick_thenGetMillisPerTick()
   {
   }

}
