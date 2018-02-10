package info.jchein.mesosphere.elevator.common.bootstrap;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PassengerBootstrap
{
   private long timeBoarded;
   private int floorBoarded;
   private int destination;
   private double weightLoad;
   
   public static PassengerBootstrap build(Consumer<PassengerBootstrapBuilder> director) {
      PassengerBootstrapBuilder bldr = PassengerBootstrap.builder();
      director.accept(bldr);
      return bldr.build();
   }
}
