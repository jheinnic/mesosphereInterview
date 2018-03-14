package test.jcop;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class BoardingEvent
{
   final double weightChange;
   final int passengersIn;
   final int passengersOut;
   
   public static BoardingEvent build(final Consumer<BoardingEventBuilder> director)
   {
      final BoardingEventBuilder bldr = BoardingEvent.builder();
      director.accept(bldr);
      return bldr.build();
   }
   
   public BoardingEvent copy(final Consumer<BoardingEventBuilder> director)
   {
      final BoardingEventBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }

   public Consumer<BoardingEventBuilder> asCloner()
   {
      return (builder) -> {
         builder.weightChange(this.weightChange)
            .passengersIn(this.passengersIn)
            .passengersOut(this.passengersOut);
      };
   }
}
