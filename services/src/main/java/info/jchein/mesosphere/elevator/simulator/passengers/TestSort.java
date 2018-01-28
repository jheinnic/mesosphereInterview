package info.jchein.mesosphere.elevator.simulator.passengers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

import info.jchein.mesosphere.elevator.domain.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.domain.model.ScheduledDrop;
import info.jchein.mesosphere.elevator.domain.model.ScheduledPickup;
import info.jchein.mesosphere.elevator.domain.model.ScheduledStop;
import info.jchein.mesosphere.elevator.domain.model.TravelPathDownComparator;
import info.jchein.mesosphere.elevator.domain.model.TravelPathUpComparator;

public class TestSort
{

   public static void main(String[] args)
   {
      final ArrayList<ScheduledStop> stopList = new ArrayList<ScheduledStop>();
      final ArrayList<Comparator<ScheduledStop>> goingUps = new ArrayList<>(5);
//      final ArrayList<Comparator<ScheduledStop>> goingDown = new ArrayList<>(5);
      for (int ii=0; ii<5; ii++) {
         stopList.add(ScheduledDrop.builder().floorIndex(ii).build());
         stopList.add(ScheduledPickup.builder().floorIndex(ii).direction(DirectionOfTravel.GOING_UP).build());
         stopList.add(ScheduledPickup.builder().floorIndex(ii).direction(DirectionOfTravel.GOING_DOWN).build());
         
         goingUps.add(new TravelPathDownComparator(ii));
      }
      
      for( int ii=0; ii<5; ii++ ) {
         System.out.println("Shuffled");
         Collections.shuffle(stopList);
         System.out.println(stopList.stream().map(item -> item.toString()).collect(Collectors.joining(" -> ")));

         System.out.println("\nGoing up from 0");
         Collections.sort(stopList, goingUps.get(0));
         System.out.println(stopList.stream().map(item -> item.toString()).collect(Collectors.joining(" -> ")));

         System.out.println("\nShuffled: ");
         Collections.shuffle(stopList);
         System.out.println(stopList.stream().map(item -> item.toString()).collect(Collectors.joining(" -> ")));

         System.out.println("\nGoing up from 1");
         Collections.sort(stopList, goingUps.get(1));
         System.out.println(stopList.stream().map(item -> item.toString()).collect(Collectors.joining(" -> ")));

         System.out.println("\nShuffled: ");
         Collections.shuffle(stopList);
         System.out.println(stopList.stream().map(item -> item.toString()).collect(Collectors.joining(" -> ")));

         System.out.println("\nGoing up from 2");
         Collections.sort(stopList, goingUps.get(2));
         System.out.println(stopList.stream().map(item -> item.toString()).collect(Collectors.joining(" -> ")));

         System.out.println("\nShuffled: ");
         Collections.shuffle(stopList);
         System.out.println(stopList.stream().map(item -> item.toString()).collect(Collectors.joining(" -> ")));

         System.out.println("\nGoing up from 3");
         Collections.sort(stopList, goingUps.get(3));
         System.out.println(stopList.stream().map(item -> item.toString()).collect(Collectors.joining(" -> ")));

         System.out.println("\nShuffled: ");
         Collections.shuffle(stopList);
         System.out.println(stopList.stream().map(item -> item.toString()).collect(Collectors.joining(" -> ")));

         System.out.println("\nGoing up from 4");
         Collections.sort(stopList, goingUps.get(4));
         System.out.println(stopList.stream().map(item -> item.toString()).collect(Collectors.joining(" -> ")));
      }
   }
}