package test.jcop;


import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.ObtainVia;
import lombok.Singular;
import lombok.Value;


@Value
@AllArgsConstructor
public class FloorLanding
{
   private final int floorIndex;

   private final ImmutableList<BoardingEvent> boardingEvents;


   @Builder(toBuilder = true)
   static FloorLanding makeFloorLanding(int floorIndex,
      @Singular("boardingEvent") @ObtainVia(method = "forBuilder")
      List<Consumer<BoardingEvent.BoardingEventBuilder>> boardingEvents)
   {
      return new FloorLanding(
         floorIndex,
         boardingEvents.stream()
            .map(BoardingEvent::build)
            .collect(
               ImmutableList::<BoardingEvent> builder,
               ImmutableList.Builder<BoardingEvent>::add,
               (builderOne, builderTwo) -> {
                  builderOne.addAll(builderTwo.build());
               })
            .build());
   }


   public static FloorLanding build(Consumer<FloorLandingBuilder> director)
   {
      final FloorLandingBuilder bldr = FloorLanding.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public FloorLanding copy(Consumer<FloorLandingBuilder> director)
   {
      final FloorLandingBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }


   Consumer<FloorLandingBuilder> asCloner()
   {
      return (builder) -> {
         builder.floorIndex(this.floorIndex);
         for (final BoardingEvent nextEvent : this.boardingEvents) {
            builder.boardingEvent(nextEvent.asCloner());
         }
      };
   }


   List<Consumer<BoardingEvent.BoardingEventBuilder>> forBuilder()
   {
      return boardingEvents.stream()
         .<Consumer<BoardingEvent.BoardingEventBuilder>> map(BoardingEvent::asCloner)
         .collect(Collectors.toList());
   }
}
