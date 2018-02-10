package test;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.physics.TravelPathDownComparator;
import info.jchein.mesosphere.elevator.common.physics.TravelPathUpComparator;
import info.jchein.mesosphere.elevator.domain.scheduler.ScheduledDrop;
import info.jchein.mesosphere.elevator.domain.scheduler.ScheduledPickup;
import info.jchein.mesosphere.elevator.domain.scheduler.ScheduledStop;


public class TestSort2
{

   public static void main(String[] args)
   {
      final ArrayList<ScheduledStop> dropList = new ArrayList<ScheduledStop>();
      final ArrayList<ScheduledStop> upList = new ArrayList<ScheduledStop>();
      final ArrayList<ScheduledStop> downList = new ArrayList<ScheduledStop>();
      final ArrayList<Comparator<ScheduledStop>> goingUps = new ArrayList<>(5);
      // final ArrayList<Comparator<ScheduledStop>> goingDown = new ArrayList<>(5);
      for (int ii = 0; ii < 5; ii++) {
         dropList.add(
            ScheduledDrop.builder()
               .floorIndex(ii)
               .build());
         upList.add(
            ScheduledPickup.builder()
               .floorIndex(ii)
               .direction(DirectionOfTravel.GOING_UP)
               .build());
         downList.add(
            ScheduledPickup.builder()
               .floorIndex(ii)
               .direction(DirectionOfTravel.GOING_DOWN)
               .build());

         goingUps.add(new TravelPathDownComparator(ii));
      }

      Comparator<ScheduledStop> comparator = goingUps.get(2);
      for (int ii = 0; ii < 5; ii++) {
         ScheduledStop dropI = dropList.get(ii);
         ScheduledStop upI = upList.get(ii);
         ScheduledStop downI = downList.get(ii);

         for (int jj = 0; jj < 5; jj++) {
            ScheduledStop dropJ = dropList.get(jj);
            ScheduledStop upJ = upList.get(jj);
            ScheduledStop downJ = downList.get(jj);

            doCompare( dropI, dropJ, comparator );
            doCompare( dropI, upJ, comparator );
            doCompare( dropI, downJ, comparator );

            doCompare( upI, dropJ, comparator );
            doCompare( upI, upJ, comparator );
            doCompare( upI, downJ, comparator );

            doCompare( downI, dropJ, comparator );
            doCompare( downI, upJ, comparator );
            doCompare( downI, downJ, comparator );
         }
      }
   }

   private static void
   doCompare(ScheduledStop stopI, ScheduledStop stopJ, Comparator<ScheduledStop> comparator)
   {
      String strI = stopI.toString();
      String strJ = stopJ.toString();
      
      int value = comparator.compare(stopI, stopJ);
      if (value < 0) {
         System.out.println(String.format("%s is before %s", strI, strJ));
      } else if (value > 0) {
         System.out.println(String.format("%s is after %s", strI, strJ));
      } else {
         System.out.println(String.format("%s is concurrent with %s", strI, strJ));
      }
   }
}
