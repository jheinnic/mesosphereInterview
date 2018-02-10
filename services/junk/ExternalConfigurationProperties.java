package info.jchein.mesosphere.elevator.common.bootstrap;


import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentProperties;
import info.jchein.mesosphere.elevator.common.bootstrap.EmulatorProperties;
import info.jchein.mesosphere.elevator.common.bootstrap.ElevatorMotorProperties;
import info.jchein.mesosphere.elevator.common.bootstrap.PendingDropoff;
import info.jchein.mesosphere.elevator.common.bootstrap.PhysicalDispatchContext;
import info.jchein.mesosphere.elevator.runtime.virtual.VirtualRuntimeProperties;


@Configuration
@ConfigurationProperties("mesosphere.elevator")
public class ExternalConfigurationProperties
{
   // @Min(3)
   public int numFloors;

   // @Min(1)
   public int numElevators;

   public double metersPerFloor = 3.5;

   public double brakeSpeed = 0.5;

   public double slowSpeed = 1.5;

   public double maxRiseSpeed = 4.0;

   public double maxDescentSpeed = 3.2;

   public double maxAcceleration = 1.5;

   public double maxJerk = 2.0;

   public int maxWeightLoad = 1250;

   public double idealWeightLoad = 600;

   public int passengerWeight = 75;

   public int minDoorHoldTimePerOpen = 3000;

   public int doorHoldTimePerPerson = 250;

   public int doorOpenCloseSlideTime = 2000;

   public int clockTickDuration = 20;

   public double brakingDistance = 300;

   public int passengerStopTolerance = 4;

   public int maxTravelTimeForHallCall = 18500;

   public double passengerPickupTimeTolerance = 30000;

   public double maxSpeedForStopAtFloor = 0.25;

   public double refusePickupAfterWeightPct = 0.85;

   public double passengerTravelTimeTolerance = 30000;


   public int getNumFloors()
   {
      return numFloors;
   }


   public int getNumElevators()
   {
      return numElevators;
   }


   public double getMetersPerFloor()
   {
      return metersPerFloor;
   }


   public double getBrakeSpeed()
   {
      return brakeSpeed;
   }


   public double getSlowSpeed()
   {
      return slowSpeed;
   }


   public double getMaxRiseSpeed()
   {
      return maxRiseSpeed;
   }


   public double getMaxDescentSpeed()
   {
      return maxDescentSpeed;
   }


   public double getMaxAcceleration()
   {
      return maxAcceleration;
   }


   public double getMaxHallCallWeightPct()
   {
      return refusePickupAfterWeightPct;
   }


   public int getMaxWeightAllowance()
   {
      return maxWeightLoad;
   }


   public int getPassengerWeight()
   {
      return passengerWeight;
   }


   public int getMaxStopsForHallCall()
   {
      return passengerStopTolerance;
   }


   public double getMaxTravelTimeForHallCall()
   {
      return maxTravelTimeForHallCall;
   }


   public int getMinDoorHoldTimePerOpen()
   {
      return minDoorHoldTimePerOpen;
   }


   public int getDoorHoldTimePerPerson()
   {
      return doorHoldTimePerPerson;
   }


   public int getDoorOpenCloseSlideTime()
   {
      return doorOpenCloseSlideTime;
   }


   public double getSimulationTickTime()
   {
      return clockTickDuration;
   }


   public double getBrakingDistance()
   {
      return brakingDistance;
   }


   public double getPassengerPickupTimeTolerance()
   {
      return passengerPickupTimeTolerance;
   }


   public double getPassengerTravelTimeTolerance()
   {
      return passengerTravelTimeTolerance;
   }


   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   EmulatorProperties getElevatorGroupConfigData(IScenarioInitializer scenarioInit)
   {
      return EmulatorProperties.build(gbldr -> {
         gbldr.motor( ElevatorMotorProperties.build( mbldr -> {
            mbldr.brakingDistance(this.brakingDistance)
               .brakingSpeed(this.brakeSpeed)
               .longAscentSpeed(this.maxRiseSpeed)
               .longDescentSpeed(this.maxDescentSpeed)
               .maxWeightLoad(this.maxWeightLoad)
               .maxAcceleration(this.maxAcceleration)
               .maxJerk(this.maxJerk);
         })).dispatch( PhysicalDispatchContext.build( dbldr -> {
            dbldr.doorHoldTimePerPerson(this.doorHoldTimePerPerson)
               .doorOpenCloseSlideTime(this.doorOpenCloseSlideTime)
               .minDoorHoldTimePerOpen(this.minDoorHoldTimePerOpen)
               .idealWeightLoad(this.idealWeightLoad)
               .passengerWeight(this.passengerWeight);
         })).building( DeploymentProperties.build( bbldr -> {
            bbldr.metersPerFloor(this.metersPerFloor)
               .carDriverKey("emulator")
               .numElevators(numElevators)
               .numFloors(numFloors);
         }));
         
         scenarioInit.accept(gbldr);
      });
   }
   
   // TODO: Migrate this to the workload profile region
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   IScenarioInitializer getInitialScenarioDirector() 
   {
      return gbldr -> {
         gbldr.car(InitialCarState.build(cbldr -> {
            cbldr.initialFloor(3)
               .weightLoaded(150)
               .requestFloor(4)
               .requestFloor(6)
               .passenger(PendingDropoff.build(pbldr -> {
                  pbldr.floorBoarded(1)
                     .timeBoarded(1000)
                     .destination(4);
               })).passenger(PendingDropoff.build(pbldr -> {
                  pbldr.floorBoarded(1)
                     .timeBoarded(1000)
                     .destination(4);
               })).passenger(PendingDropoff.build(pbldr -> {
                  pbldr.floorBoarded(1)
                     .timeBoarded(1000)
                     .destination(6);
               }));
          })).car(InitialCarState.build(cbldr -> {
            cbldr.initialFloor(3)
               .weightLoaded(150)
               .requestFloor(4)
               .requestFloor(6)
               .passenger(PendingDropoff.build(pbldr -> {
                  pbldr.floorBoarded(1)
                     .timeBoarded(1000)
                     .destination(4);
               })).passenger(PendingDropoff.build(pbldr -> {
                  pbldr.floorBoarded(1)
                     .timeBoarded(1000)
                     .destination(4);
               })).passenger(PendingDropoff.build(pbldr -> {
                  pbldr.floorBoarded(1)
                     .timeBoarded(1000)
                     .destination(6);
               }));
          })).car(InitialCarState.build(cbldr -> {
            cbldr.initialFloor(3)
               .weightLoaded(150)
               .requestFloor(4)
               .requestFloor(6)
               .passenger(PendingDropoff.build(pbldr -> {
                  pbldr.floorBoarded(1)
                     .timeBoarded(1000)
                     .destination(4);
               })).passenger(PendingDropoff.build(pbldr -> {
                  pbldr.floorBoarded(1)
                     .timeBoarded(1000)
                     .destination(4);
               })).passenger(PendingDropoff.build(pbldr -> {
                  pbldr.floorBoarded(1)
                     .timeBoarded(1000)
                     .destination(6);
               }));
          }));
      };
   }


   /*
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   PassengerToleranceProperties getPassengerTolerance()
   {
      return PassengerToleranceProperties.build(builder -> {
         builder.passengerPickupTimeTolerance(this.passengerPickupTimeTolerance)
            .passengerTravelTimeTolerance(this.passengerTravelTimeTolerance)
            .passengerStopTolerance(this.passengerStopTolerance)
            .refusePickupAfterWeightPct(this.refusePickupAfterWeightPct);
      });
   }
   */
}
