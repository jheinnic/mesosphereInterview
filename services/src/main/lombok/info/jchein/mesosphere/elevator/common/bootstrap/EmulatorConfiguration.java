package info.jchein.mesosphere.elevator.common.bootstrap;


import java.util.function.Consumer;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.monitor.model.PendingPickup;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.FieldDefaults;


@Validated
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
/*
@ScriptAssert.List({
   @ScriptAssert(lang = "javascript", alias = "_this",
      script = "_this.pendingPickups.every( (p) => p.pickupFloor < _this.numFloors && p.pickupFloor >= 0 )"),
   @ScriptAssert(lang = "javascript", alias = "_this",
      script = "_this.pendingPickups.every( (p) => p.dropOffFloor < _this.numFloors && p.dropOffFloor >= 0 )"),
   @ScriptAssert(lang = "javascript", alias = "_this",
      script = "_this.cars.every( (c) => c.initialFloor < _this.numFloors && c.initialFloor >= 0 )"),
   @ScriptAssert(lang = "javascript", alias = "_this",
      script = "_this.cars.every( (c) => c.passengers.every( (p) => p.pickupFloor < this._numFloors && p.pickupFloor >= 0 ) )"),
   @ScriptAssert(lang = "javascript", alias = "_this",
      script = "_this.cars.every( (c) => c.passengers.every( (p) => p.dropOffFloor < this._numFloors && p.dropOffFloor >= 0 ) )"),
   @ScriptAssert(lang = "javascript", alias = "_this",
      script = "_this.cars.every( (c) => c.passengers.every( (p) => p.callTime <= p.pickupTime ) )")
})
*/
public class EmulatorConfiguration
{
   @Min(0)
   public final int numFloors;

   @NotNull
   @NotBlank
   public final String driverAlias;

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
