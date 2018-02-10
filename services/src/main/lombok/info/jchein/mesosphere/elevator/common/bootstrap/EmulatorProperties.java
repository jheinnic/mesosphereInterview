package info.jchein.mesosphere.elevator.common.bootstrap;


import java.util.LinkedList;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.ScriptAssert;
import org.springframework.boot.context.properties.ConfigurationProperties;

import info.jchein.mesosphere.validator.annotation.Positive;
import lombok.Data;


/**
 * Mutable view of Emulator configuration state, implemented for reuse by Spring's ConfigurationProperties feature set.  Not intended for reuse outside of
 * {@link Configuration} annotated classes, where it is intended to be converted to an immutable variant by invocation providing it as argument to
 * {@link EmulatorConfiguration#toImmutable(EmulatorProperties)}, which returns its immutable equivalent.
 * 
 * @author jheinnic
 */
@Data
@ScriptAssert.List({
   @ScriptAssert(lang="javascript", alias="_this", script="_this.pendingPickups.every( (p) => p.pickupFloor < _this.numFloors && p.pickupFloor >= 0 )"),
   @ScriptAssert(lang="javascript", alias="_this", script="_this.pendingPickups.every( (p) => p.dropOffFloor < _this.numFloors && p.dropOffFloor >= 0 )"),
   @ScriptAssert(lang="javascript", alias="_this", script="_this.pendingPickups.every( (p) => p.callTime <= p.pickupTime )"),
   @ScriptAssert(lang="javascript", alias="_this", script="_this.cars.every( (c) => c.initialFloor < _this.numFloors && c.initialFloor >= 0 )"),
   @ScriptAssert(lang="javascript", alias="_this", script="_this.cars.every( (c) => c.passengers.every( (p) => p.pickupFloor < this._numFloors && p.pickupFloor >= 0 ) )"),
   @ScriptAssert(lang="javascript", alias="_this", script="_this.cars.every( (c) => c.passengers.every( (p) => p.dropOffFloor < this._numFloors && p.dropOffFloor >= 0 ) )"),
   @ScriptAssert(lang="javascript", alias="_this", script="_this.cars.every( (c) => c.passengers.every( (p) => p.callTIme <= p.pickupTime ) )")
})
@ConfigurationProperties("mesosphere.elevator.emulator")
public class EmulatorProperties
{
   @Min(0)
   public int numFloors;
   
   @Valid
   @NotNull
   @Size(min = 0)
   public final LinkedList<PendingPickup> pendingPickups = new LinkedList<>();

   @Valid
   @NotNull
   @Size(min = 1)
   public final LinkedList<InitialCarState> cars = new LinkedList<>();


   @Data
   public class InitialCarState
   {
      @Min(0)
      public int initialFloor;

      @Valid
      @NotNull
      @Size(min=0)
      public final LinkedList<PendingDropOff> passengers = new LinkedList<PendingDropOff>();
   }


   @Data
   public class PendingDropOff
   {
      @Min(0)
      public long callTime;
      @Min(0)
      public long pickupTime;
      @Min(0)
      public int pickupFloor;
      @Min(0)
      public int dropOffFloor;
      @Positive
      public double weight;
   }


   @Data
   public class PendingPickup
   {
      @Min(0)
      public long callTime;
      @Min(0)
      public int pickupFloor;
      @Min(0)
      public int dropOffFloor;
      @Positive
      public double weight;
   }
}
