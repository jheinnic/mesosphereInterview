package info.jchein.mesosphere.elevator.emulator.model;

import java.util.concurrent.TimeUnit;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentConfiguration;
import info.jchein.mesosphere.elevator.common.bootstrap.InitialCarState;
import info.jchein.mesosphere.elevator.common.physics.IElevatorPhysicsService;
import info.jchein.mesosphere.elevator.common.physics.JourneyArc;
import info.jchein.mesosphere.elevator.common.physics.PathMoment;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarDriver;
import info.jchein.mesosphere.elevator.control.sdk.IElevatorCarPort;
import info.jchein.mesosphere.elevator.control.sdk.Priorities;
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeScheduler;

//@Component
//@Scope(IElevatorCarScope.SCOPE_NAME)
public class EmulatedElevatorCar implements IElevatorCarDriver {
   private static final double MARGIN_OF_ERROR = 0.00000001;

   private final IElevatorPhysicsService physicsService;
   private final IElevatorCarPort port;
   private final IRuntimeScheduler scheduler;
   private final DeploymentConfiguration deployConfig;
   private final long doorOpenCloseTime;

   private PathMoment brakeMoment;
   private PathMoment landingMoment;
	
	public EmulatedElevatorCar(
	   IElevatorCarPort port, IRuntimeScheduler scheduler, IElevatorPhysicsService physics, DeploymentConfiguration deployConfig) 
	{
      this.port = port;
      this.scheduler = scheduler;
      this.physicsService = physics;
      this.deployConfig = deployConfig;
      this.doorOpenCloseTime = Math.round(Math.ceil(1000 * this.deployConfig.getDoors().getOpenCloseTime()));
	}
	
	public void setInitialState(InitialCarState initialData) {
	   this.port.bootstrapDriver(initialData);
	}

   @Override
   public void openDoors(DirectionOfTravel direction)
   {
      this.scheduler.scheduleOnce(
         this.doorOpenCloseTime, 
         TimeUnit.MILLISECONDS, 
         Priorities.OPEN_DOORS.getValue(), 
         this.port::passengerDoorsOpened);
   }

   @Override
   public void closeDoors()
   {
      this.scheduler.scheduleOnce(
         this.doorOpenCloseTime, 
         TimeUnit.MILLISECONDS, 
         Priorities.CLOSE_DOORS.getValue(), 
         this.port::passengerDoorsClosed);
   }
   

   @Override
   public JourneyArc dispatchTo(int toFloorIndex)
   {
      // TODO: Alter the physics service to allow arbitrary initial heights
      final JourneyArc retVal =
         this.physicsService.getTraversalPath(this.port.getCurrentFloorIndex(), toFloorIndex);
      this.brakeMoment = retVal.getBrakeAppliedMoment();
      this.landingMoment = retVal.getTerminalMoment();

      return retVal;
   }

   @Override
   public void slowForArrival()
   {
      double currentHeight = this.port.getExpectedLocation();
      double targetHeight = this.brakeMoment.getHeight();
      if (Math.abs(currentHeight - targetHeight) < MARGIN_OF_ERROR) {
         double secondsToLand = this.landingMoment.getTime() - this.brakeMoment.getTime();
         long millisToLand = Math.round(1000 * secondsToLand);
         this.scheduler.scheduleOnce(
            millisToLand,
            TimeUnit.MILLISECONDS, 
            Priorities.SCHEDULE_LANDING.getValue(), 
            this.port::parkedAtLanding);
      } else {
         throw new IllegalStateException(
            String.format("Current height (%f) and target height (%f) are too far appart to begin braking",
               currentHeight, targetHeight));
      }
   } 
}
