package info.jchein.mesosphere.elevator.common.bootstrap;


import java.util.function.Consumer;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.ScriptAssert;
import org.springframework.validation.annotation.Validated;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.common.PendingPickup;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;


@Value
@Validated
@Builder(toBuilder = true)
@ScriptAssert.List({
   @ScriptAssert(lang = "javascript", alias = "_this",
      script = "_this.pendingPickups.every( (p) => p.pickupFloor < _this.numFloors && p.pickupFloor >= 0 )"),
   @ScriptAssert(lang = "javascript", alias = "_this",
      script = "_this.pendingPickups.every( (p) => p.dropoffFloor < _this.numFloors && p.dropoffFloor >= 0 )"),
   @ScriptAssert(lang = "javascript", alias = "_this",
      script = "_this.pendingPickups.every( (p) => p.callTime <= p.pickupTime )"),
   @ScriptAssert(lang = "javascript", alias = "_this",
      script = "_this.cars.every( (c) => c.initialFloor < _this.numFloors && c.initialFloor >= 0 )"),
   @ScriptAssert(lang = "javascript", alias = "_this",
      script = "_this.cars.every( (c) => c.passengers.every( (p) => p.pickupFloor < this._numFloors && p.pickupFloor >= 0 ) )"),
   @ScriptAssert(lang = "javascript", alias = "_this",
      script = "_this.cars.every( (c) => c.passengers.every( (p) => p.dropoffFloor < this._numFloors && p.dropoffFloor >= 0 ) )"),
   @ScriptAssert(lang = "javascript", alias = "_this",
      script = "_this.cars.every( (c) => c.passengers.every( (p) => p.callTIme <= p.pickupTime ) )")
})
public class EmulatorConfiguration
{
   @Min(0)
   public final int numFloors;

   @Valid
   @NotNull
   @Singular
   @Size(min = 0)
   public final ImmutableList<PendingPickup> pendingPickups;

   @Valid
   @NotNull
   @Singular
   @Size(min = 1)
   public final ImmutableList<InitialCarState> cars;


   public static EmulatorConfiguration build(Consumer<EmulatorConfigurationBuilder> director)
   {
      EmulatorConfigurationBuilder bldr = EmulatorConfiguration.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public EmulatorConfiguration copy(Consumer<EmulatorConfigurationBuilder> director)
   {
      EmulatorConfigurationBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
