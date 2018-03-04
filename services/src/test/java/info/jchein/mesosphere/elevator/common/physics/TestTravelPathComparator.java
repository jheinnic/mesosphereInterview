package info.jchein.mesosphere.elevator.common.physics;


import java.util.ArrayList;o
import java.util.Collection;

import org.assertj.core.api.AbstractComparableAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.control.manifest.ScheduledStop;
import static fixtures.elevator.common.physics.ComparisonExpectation.*;
import static fixtures.elevator.common.physics.ScheduledStopFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class TestTravelPathComparator
{

   @Parameters
   public static Collection<Object[]> data()
   {
      return new ArrayList<Object[]>() {
         {
            add( new Object[] { 0, UP, UP_UP_DUAL[0], UP_UP_DUAL[0], IS_EQUAL_TO });
            add( new Object[] { 0, UP, UP_UP_DUAL[0], UP_UP_DUAL[0], 0 });
//            add( new Object[] { 1, 1 });
//            add( new Object[] { 2, 1 });
//            add( new Object[] { 3, 2 });
//            add( new Object[] { 4, 3 });
//            add( new Object[] { 5, 5 });
//            add( new Object[] { 6, 8 });
         }
      };
   }


   private int originalFloor;
   private DirectionOfTravel originalDirection;
   private ScheduledStop stopA;
   private ScheduledStop stopB;
   private int expected;


   public TestTravelPathComparator( int originalFloor, DirectionOfTravel originalDirection, ScheduledStop stopA, ScheduledStop stopB, int expected )
   {
      this.originalFloor = originalFloor;
      this.originalDirection = originalDirection;
      this.stopA = stopA;
      this.stopB = stopB;
      this.expected = expected;
   }


   @Test
   public void test()
   {
      TravelPathComparator sut = new TravelPathComparator(this.originalDirection, this.originalFloor, NUM_FLOORS);
      AbstractComparableAssert<?, ScheduledStop> usingComparator = assertThat(this.stopA).as("%s compared to %s").usingComparator(sut);

      switch(expected) {
         case -1: {
            usingComparator.isLessThan(this.stopB);
            break;
         }
         case 1: {
            usingComparator.isGreaterThan(this.stopB);
            break;
         }
         case 0: {
            usingComparator.isEqualTo(this.stopB);
            break;
         }
      }
   }
}
