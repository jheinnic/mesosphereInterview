package info.jchein.mesosphere.elevator.emulator.model;


import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.bootstrap.EmulatorConfiguration;
import info.jchein.mesosphere.elevator.common.physics.JourneyArc;
import info.jchein.mesosphere.elevator.emulator.model.IEmulatorControl;
import info.jchein.mesosphere.elevator.emulator.model.IEmulatorRoot;
import info.jchein.mesosphere.elevator.emulator.model.IManifestUpdate;
import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;

import java.util.BitSet;
import java.util.function.Consumer;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@SuppressWarnings("all")
public class ElevatorEmulator
implements IEmulatorRoot, IEmulatorControl
{
   @NotNull
   private final IRuntimeClock clock;

   @NotNull
   private final IRuntimeEventBus eventBus;

   @NotNull
   private final IRuntimeScheduler scheduler;

   @NotNull
   private final BitSet ascendingCalls;

   @NotNull
   private final BitSet descendingCalls;

   private EmulatorConfiguration emulatorConfiguration;

   @Autowired
   public ElevatorEmulator( @NotNull final IRuntimeClock clock,
      @NotNull final IRuntimeScheduler scheduler, @NotNull final IRuntimeEventBus eventBus,
      @NotNull final EmulatorConfiguration emulatorProps )
   {
      this.clock = clock;
      this.eventBus = eventBus;
      this.scheduler = scheduler;
      this.emulatorConfiguration = this.emulatorConfiguration;
      this.ascendingCalls = new BitSet();
      this.descendingCalls = new BitSet();
   }


   @Override
   public void callForPickup(final int floorIndex, final DirectionOfTravel direction)
   {
      throw new UnsupportedOperationException("TODO: auto-generated method stub");
   }


   @Override

   public void onArrival(final int floorIndex, final DirectionOfTravel nextDirection)
   {
      throw new UnsupportedOperationException("TODO: auto-generated method stub");
   }


   @Override

   public void onPathUpdated(

      final JourneyArc travaaaaelArc,

      final DirectionOfTravel nextDirection)
   {
      throw new UnsupportedOperationException("TODO: auto-generated method stub");
   }


   @Override
   public void blockUntil(final long clockTime)
   {
      throw new UnsupportedOperationException("TODO: auto-generated method stub");
   }


   @Override
   public void updateManifest(final int carIndex, final int floorIndex,
      final DirectionOfTravel direction, final Consumer<IManifestUpdate> director)
   {
      throw new UnsupportedOperationException("TODO: auto-generated method stub");
   }
}
