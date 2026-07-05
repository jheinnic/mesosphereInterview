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
 * Pairwise compareTo exercises for the travelIndex-based ScheduledStop ordering.
 *
 * For every pair (i, j) of stops we print the ordering relationship and also verify
 * antisymmetry: compare(i,j) and compare(j,i) must have opposite signs (or both zero).
 */
public class TestSort2
{
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
      @Override public void bootstrapFromState(int initialFloor, ImmutableList<PendingDropOff> passengers) { }
   }

   public static void main(String[] args)
   {
      final int NUM_FLOORS = 10;
      final ManifestStub manifest = new ManifestStub(NUM_FLOORS);

      // One list per stop type across passes 0 (up) and 1 (down) for floors 1-4.
      final ArrayList<PassengerManifest.ScheduledStop> upDrops   = new ArrayList<>();
      final ArrayList<PassengerManifest.ScheduledStop> upCalls   = new ArrayList<>();
      final ArrayList<PassengerManifest.ScheduledStop> downDrops = new ArrayList<>();
      final ArrayList<PassengerManifest.ScheduledStop> downCalls = new ArrayList<>();

      for (int floor = 1; floor <= 4; floor++) {
         upDrops  .add(manifest.new ScheduledDropOff(floor, 0));
         upCalls  .add(manifest.new ScheduledPickUp(floor, 0));
         downDrops.add(manifest.new ScheduledDropOff(floor, 1));
         downCalls.add(manifest.new ScheduledPickUp(floor, 1));
      }

      PassengerManifest.ScheduledStop upReversal   = manifest.new ScheduledReversal(NUM_FLOORS - 1, 0);
      PassengerManifest.ScheduledStop downReversal = manifest.new ScheduledReversal(0, 1);

      // Build a flat list for pairwise comparison: drops, calls, reversal — both passes.
      final ArrayList<PassengerManifest.ScheduledStop> all = new ArrayList<>();
      all.addAll(upDrops);
      all.addAll(upCalls);
      all.add(upReversal);
      all.addAll(downDrops);
      all.addAll(downCalls);
      all.add(downReversal);

      int violations = 0;

      System.out.println("=== Pairwise compareTo results ===");
      for (int i = 0; i < all.size(); i++) {
         PassengerManifest.ScheduledStop si = all.get(i);
         for (int j = 0; j < all.size(); j++) {
            PassengerManifest.ScheduledStop sj = all.get(j);

            int fwd = si.compareTo(sj);
            int rev = sj.compareTo(si);

            String rel;
            if      (fwd < 0) rel = "before";
            else if (fwd > 0) rel = "after";
            else              rel = "concurrent with";

            System.out.printf("  %s  %-30s  %s%n", si, rel, sj);

            // Antisymmetry check: sign(fwd) must equal -sign(rev).
            if (Integer.signum(fwd) != -Integer.signum(rev)) {
               System.out.printf("    *** ANTISYMMETRY VIOLATION: fwd=%d rev=%d%n", fwd, rev);
               violations++;
            }
         }
      }

      System.out.printf("%n=== Sorted order ===%n");
      Collections.sort(all);
      System.out.println(all.stream().map(Object::toString).collect(Collectors.joining("\n  ")));

      System.out.printf("%n%d antisymmetry violation(s) detected.%n", violations);
   }
}
