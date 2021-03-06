package info.jchein.mesosphere.elevator.common.bootstrap;


import java.util.function.Consumer;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.monitor.model.PendingDropOff;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.FieldDefaults;


@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class InitialCarState
{
   @Min(0)
   public int initialFloor;

   @Valid
   @NotNull
   @Size(min = 0)
   @Singular
   public final ImmutableList<PendingDropOff> passengers;


   public static InitialCarState build(Consumer<InitialCarStateBuilder> director)
   {
      InitialCarStateBuilder bldr = InitialCarState.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public InitialCarState copy(Consumer<InitialCarStateBuilder> director)
   {
      InitialCarStateBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
