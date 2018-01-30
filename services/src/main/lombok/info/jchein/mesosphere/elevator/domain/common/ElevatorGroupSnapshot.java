package info.jchein.mesosphere.elevator.domain.common;

import java.util.BitSet;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder(toBuilder=true)
public class ElevatorGroupSnapshot
{
   final long clockTime;

//   @ObtainVia(method="getUpwardPickups")
   private final BitSet upwardPickups;

//   @ObtainVia(method="getDownwardPickups")
   private final BitSet downwardPickups;
   
   @Singular
   final ImmutableList<ElevatorCarSnapshot> cars;
   
   public static ElevatorGroupSnapshot build(Consumer<ElevatorGroupSnapshotBuilder> director) {
      ElevatorGroupSnapshotBuilder bldr = ElevatorGroupSnapshot.builder();
      director.accept(bldr);
      return bldr.build();
   }
   
   public ElevatorGroupSnapshot copy(Consumer<ElevatorGroupSnapshotBuilder> director, Function<ElevatorCarSnapshot, ElevatorCarSnapshot> carMap)
   {
      final ElevatorGroupSnapshotBuilder bldr = ElevatorGroupSnapshot.builder()
         .upwardPickups(this.upwardPickups)
         .downwardPickups(this.downwardPickups);
      this.cars.stream().sequential().forEach(carSnap -> {
         bldr.car(carMap.apply(carSnap));
      });
      director.accept(bldr);
      return bldr.build();
   }
   
   
   public ElevatorGroupSnapshot copy(Consumer<ElevatorGroupSnapshotBuilder> director) 
   {
      final ElevatorGroupSnapshotBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }

   public BitSet getUpwardPickups() {
      return (BitSet) this.upwardPickups.clone();
   }

   public BitSet getDownwardPickups() {
      return (BitSet) this.downwardPickups.clone();
   }
}
