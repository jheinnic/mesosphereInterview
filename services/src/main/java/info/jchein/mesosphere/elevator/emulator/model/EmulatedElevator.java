package info.jchein.mesosphere.elevator.emulator.model;


import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentConfiguration;
import info.jchein.mesosphere.elevator.common.bootstrap.EmulatorConfiguration;
import info.jchein.mesosphere.elevator.common.bootstrap.InitialCarState;
import info.jchein.mesosphere.elevator.common.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.control.IElevatorCarScope;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarPort;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorLandingsPort;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeScheduler;


@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class EmulatedElevator
implements IEmulatorControl
{
   @NotNull
   private final IRuntimeClock clock;

   @NotNull
   private final IRuntimeEventBus eventBus;

   @NotNull
   private final IRuntimeScheduler scheduler;
   
   @NotNull
   private final IElevatorLandingsPort port;

   @NotNull
   private final IElevatorPhysicsService physicsService;
   
   @NotNull
   private final EmulatedElevatorCar[] emulatedCars;
   
   @NotNull
   private final EmulatorConfiguration emulatorConfiguration;
   
   @NotNull
   private final DeploymentConfiguration deploymentConfiguration;

   @NotNull
   private final IElevatorCarScope elevatorCarScope;

   @NotNull
   private final ManifestUpdateSupplier manifestUpdateFactory;

   @Autowired
   public EmulatedElevator( @NotNull IElevatorLandingsPort port, @NotNull final IRuntimeClock clock,
      @NotNull final IRuntimeScheduler scheduler, @NotNull final IRuntimeEventBus eventBus,
      @NotNull final IElevatorPhysicsService physicsService,
      @NotNull final IElevatorCarScope elevatorCarScope,
      @NotNull final ManifestUpdateSupplier manifestUpdateFactory,
      @NotNull final DeploymentConfiguration deploymentConfiguration,
      @NotNull final EmulatorConfiguration emulatorConfiguration )
   {
      this.port = port;
      this.clock = clock;
      this.eventBus = eventBus;
      this.scheduler = scheduler;
      this.physicsService = physicsService;
      this.elevatorCarScope = elevatorCarScope;
      this.manifestUpdateFactory = manifestUpdateFactory;
      this.deploymentConfiguration = deploymentConfiguration;
      this.emulatorConfiguration = emulatorConfiguration;

      this.emulatedCars = new EmulatedElevatorCar[deploymentConfiguration.getBuilding().getNumElevators()];
   }

   
   public EmulatedElevatorCar getCarEmulator(IElevatorCarPort carPort) 
   {
      final int carIndex = carPort.getCarIndex();
      Preconditions.checkState(this.emulatedCars[carIndex] == null);
      this.emulatedCars[carIndex] = new EmulatedElevatorCar(
         carPort, this.scheduler, this.physicsService, this.deploymentConfiguration);
      this.emulatedCars[carIndex].setInitialState(InitialCarState.builder().build());
      return this.emulatedCars[carIndex];
   }

   @Override
   public void callForPickup(final int floorIndex, final DirectionOfTravel direction)
   {
      this.port.callForPickup(floorIndex, direction);
   }


   @Override
   public void blockUntil(final long clockTime)
   {
      this.clock.advanceBy(clockTime - this.clock.now(), TimeUnit.MILLISECONDS);
   }


   @Override
   public void updateManifest(final int carIndex, final int floorIndex,
      final DirectionOfTravel direction, final Consumer<IManifestUpdate> director)
   {
      final ManifestUpdate updater = 
         this.elevatorCarScope.<ManifestUpdate>evalForCar(carIndex, this.manifestUpdateFactory);
      director.accept(updater);
      updater.post();
   }
}
