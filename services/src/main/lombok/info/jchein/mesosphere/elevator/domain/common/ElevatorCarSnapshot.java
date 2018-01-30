package info.jchein.mesosphere.elevator.domain.common;

import java.util.BitSet;
import java.util.function.Consumer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder=true)
public class ElevatorCarSnapshot {
   final int carIndex;
   final BitSet stopsRequested;
   final BitSet upwardPickups;
   final BitSet downwardPickups;
   final ServiceLifecycleStage stage;
   final SpeedOfTravel speed;
   final DirectionOfTravel direction;
   final double weightLoad;
   final int latestFloorIndex;
   final int currentDestination;
   final int nextDestination;
   final int reverseTravelFloor;
   
   public static ElevatorCarSnapshot build(Consumer<ElevatorCarSnapshotBuilder> director) {
      ElevatorCarSnapshotBuilder bldr = ElevatorCarSnapshot.builder();
      director.accept(bldr);
      return bldr.build();
   }

   public ElevatorCarSnapshot copy(Consumer<ElevatorCarSnapshotBuilder> director) {
      ElevatorCarSnapshotBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}