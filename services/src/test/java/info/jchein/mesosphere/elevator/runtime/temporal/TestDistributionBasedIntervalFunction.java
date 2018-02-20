package info.jchein.mesosphere.elevator.runtime.temporal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import info.jchein.mesosphere.elevator.runtime.IIntervalHandler;
import info.jchein.mesosphere.elevator.runtime.PoissonProcessFunction;


@RunWith(MockitoJUnitRunner.class)
public class TestDistributionBasedIntervalFunction
{

   public static double FIXTURE_SAMPLE_VALUE_ONE = 5.5;
   public static double FIXTURE_SAMPLE_VALUE_TWO = 12.4;

   public static long FIXTURE_CONVERTED_VALUE_ONE = 5500;
   public static long FIXTURE_CONVERTED_VALUE_TWO = 12400;

   @InjectMocks
   private PoissonProcessFunction sut;
   
   @Mock
   private IIntervalHandler mockHandler;

   @Mock
   private ExponentialDistribution mockDist;


   @Test
   public void givenSut_whenApply_thenSample()
   {
//      ExponentialDistribution mockDist = mock(ExponentialDistribution.class);
//      FixtureDistributionIntervalHandler fixture = new FixtureDistributionIntervalHandler();
//      sut = new PoissonProcessFunction(mockDist, fixture);

      when(mockDist.sample()).thenReturn(FIXTURE_SAMPLE_VALUE_ONE)
         .thenReturn(FIXTURE_SAMPLE_VALUE_TWO);

      assertThat(sut.apply(1000L))
         .as("Function's first return should be %f", FIXTURE_SAMPLE_VALUE_ONE)
         .isEqualTo(FIXTURE_CONVERTED_VALUE_ONE);
      assertThat(sut.apply(1000L))
         .as("Function's second return should be %f", FIXTURE_SAMPLE_VALUE_TWO)
         .isEqualTo(FIXTURE_CONVERTED_VALUE_TWO);
   }

}
