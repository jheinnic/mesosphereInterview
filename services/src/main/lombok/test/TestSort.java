package test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import org.jgrapht.alg.util.Pair;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.control.PerformanceEstimate;
import info.jchein.mesosphere.elevator.control.PickupImpactEstimate;
import info.jchein.mesosphere.elevator.control.manifest.ITravelGraph;
import info.jchein.mesosphere.elevator.control.manifest.PassengerManifest;
import info.jchein.mesosphere.elevator.monitor.model.PendingDropOff;


/**
 * Exercises the travelIndex-based sort order of PassengerManifest's scheduled-stop inner classes.
 *
 * Stops created for pass 0 (even = upward) sort before stops for pass 1 (odd = downward),
 * which sort before pass 2, etc.  Within a single pass, floors ascend (even) or descend (odd).
 * At the same floor and pass, drop-offs sort before pickups, and pickups before reversals.
 */
public class TestSort
{
   // Minimal concrete subclass — only allocateTravelGraph is exercised during sort tests,
   // so every other unimplemented IPassengerManifest method just stubs to a safe default.
   static final class ManifestStub extends PassengerManifest {
      ManifestStub(int numFloors) { super(numFloors); }

      @Override protected ITravelGraph allocateTravelGraph(DirectionOfTravel direction) { return null; }
      @Override public int getCurrentFloor() { return 0; }
      @Override public boolean hasCurrentFloorStopRequest() { return false; }
      @Override public BitSet getFloorStops() { return new BitSet(); }
      @Override public PerformanceEstimate estimatePerformance() { return null; }
      @Override public PickupImpactEstimate estimatePickupImpact(
            int floorIndex, DirectionOfTravel direction,
            ImmutableList<Pair<Integer, Double>> passengers) { return null; }

      @Override
      public void bootstrapFromState(int initialFloor, ImmutableList<PendingDropOff> passengers) {
         // TODO Auto-generated method stub
         throw new UnsupportedOperationException("Unimplemented method 'bootstrapFromState'");
      }
   }

   public static void main(String[] args)
   {
      final int NUM_FLOORS = 10;
      final ManifestStub manifest = new ManifestStub(NUM_FLOORS);

      // Build a list that mixes stops from three consecutive passes.
      // Pass 0 (upward):   pickups and drops at floors 1-4, reversal at floor 9
      // Pass 1 (downward): pickups and drops at floors 1-8, reversal at floor 0
      // Pass 2 (upward):   pickups at floors 2-6
      final ArrayList<PassengerManifest.ScheduledStop> stopList = new ArrayList<>();

      for (int floor = 1; floor <= 4; floor++) {
         stopList.add(manifest.new ScheduledPickUp(floor, 0));
         stopList.add(manifest.new ScheduledDropOff(floor, 0));
      }
      stopList.add(manifest.new ScheduledReversal(NUM_FLOORS - 1, 1));

      for (int floor = 1; floor <= 8; floor++) {
         stopList.add(manifest.new ScheduledPickUp(floor, 1));
         stopList.add(manifest.new ScheduledDropOff(floor, 1));
      }
      stopList.add(manifest.new ScheduledReversal(0, 2));

      for (int floor = 2; floor <= 6; floor++) {
         stopList.add(manifest.new ScheduledPickUp(floor, 2));
      }

      // Same-floor tie-breaking scenario: drop and pickup both at floor 3, pass 0.
      stopList.add(manifest.new ScheduledDropOff(3, 0));
      stopList.add(manifest.new ScheduledPickUp(3, 0));

      // Shuffle and sort several times to confirm stable ordering.
      for (int round = 1; round <= 3; round++) {
         System.out.printf("%n=== Round %d: Shuffled ===%n", round);
         Collections.shuffle(stopList);
         System.out.println(
            stopList.stream().map(Object::toString).collect(Collectors.joining("  |  ")));

         System.out.printf("%n=== Round %d: Sorted ===%n", round);
         Collections.sort(stopList);
         System.out.println(
            stopList.stream().map(Object::toString).collect(Collectors.joining("\n  ")));
      }
   }
}
