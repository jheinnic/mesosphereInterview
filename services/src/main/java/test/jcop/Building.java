package test.jcop;


import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.ObtainVia;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Tolerate;


@Data
@AllArgsConstructor()
public class Building
{
   private final int numFloors;
   private DirectionOfTravel currentDirection;
   private ImmutableList<FloorLanding> floorLandings;


   @Builder(toBuilder=true)
   public static Building makeBuilding(
      int numFloors, DirectionOfTravel currentDirection,
      @Singular("floorLanding") @ObtainVia(method="forBuilder")
      List<Consumer<FloorLanding.FloorLandingBuilder>> floorLandings)
   {
      return new Building(
         numFloors,
         currentDirection,
         floorLandings.stream()
            .map(FloorLanding::build)
            .collect(
               ImmutableList::<FloorLanding> builder,
               ImmutableList.Builder<FloorLanding>::add,
               (builderOne, builderTwo) -> {
                  builderOne.addAll(builderTwo.build());
               })
            .build());
   }

   public static Building build(Consumer<BuildingBuilder> director)
   {
      final BuildingBuilder bldr = Building.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public Building copy(Consumer<BuildingBuilder> director)
   {
      final BuildingBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }

   @Tolerate
   private List<Consumer<FloorLanding.FloorLandingBuilder>> forBuilder() {
      return floorLandings.stream()
         .<Consumer<FloorLanding.FloorLandingBuilder>>map(FloorLanding::asCloner)
         .collect(Collectors.toList());
   }

}
