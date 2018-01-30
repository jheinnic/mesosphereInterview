package info.jchein.mesosphere.elevator.runtime;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.commons.math3.distribution.RealDistribution;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
class TestDistributionBasedIntervalFunction
{

   public static double FIXTURE_SAMPLE_VALUE_ONE = 5.5;
   public static double FIXTURE_SAMPLE_VALUE_TWO = 12.4;
   @InjectMocks
   private FixtureDistributionIntervalHandler sut;

   @Mock
   private RealDistribution mockDist;


   @Test
   void givenSut_whenApply_thenPrng()
   {
      RealDistribution mockDist = mock(RealDistribution.class);

      FixtureDistributionIntervalHandler sut = new FixtureDistributionIntervalHandler(mockDist);

      when(mockDist.sample()).thenReturn(FIXTURE_SAMPLE_VALUE_ONE)
         .thenReturn(FIXTURE_SAMPLE_VALUE_TWO);

      assertThat(sut.apply(1000L))
         .as("Function's first return should be %f", FIXTURE_SAMPLE_VALUE_ONE)
         .isEqualTo(Math.round(FIXTURE_SAMPLE_VALUE_ONE));
      assertThat(sut.apply(1000L))
         .as("Function's second return should be %f", FIXTURE_SAMPLE_VALUE_ONE)
         .isEqualTo(Math.round(FIXTURE_SAMPLE_VALUE_TWO));
   }

}
