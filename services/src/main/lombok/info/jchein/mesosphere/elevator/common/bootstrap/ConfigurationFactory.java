package info.jchein.mesosphere.elevator.common.bootstrap;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.common.IValueFactory;
import info.jchein.mesosphere.elevator.common.PendingDropOff;
import info.jchein.mesosphere.elevator.common.PendingPickup;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
class ConfigurationFactory implements IConfigurationFactory
{
   private final IValueFactory idFactory;

   @Autowired
   ConfigurationFactory( @NotNull IValueFactory idFactory ) {
      this.idFactory = idFactory;
   }

   @Override
   public EmulatorConfiguration hardenEmulatorConfig(EmulatorProperties mutable)
   {
      return EmulatorConfiguration.build(bldr -> { bldr.numFloors(mutable.numFloors)
         .pendingPickups(mutable.pendingPickups.stream()
            .<ImmutableList.Builder<PendingPickup>> collect(
               ImmutableList::<PendingPickup> builder,
               (ImmutableList.Builder<PendingPickup> lBldr, EmulatorProperties.PendingPickup pickup) -> {
                  lBldr.add(PendingPickup.build(pbldr -> {
                     pbldr.id(idFactory.getNextPassengerId())
                        .callTime(pickup.callTime)
                        .dropOffFloor(pickup.dropOffFloor)
                        .pickupFloor(pickup.pickupFloor)
                        .weight(pickup.weight);
                  }));
               },
               (left, right) -> left.addAll(right.build())
            ).build()
         ).cars(mutable.cars.stream()
            .<ImmutableList.Builder<InitialCarState>> collect(
               ImmutableList::<InitialCarState> builder,
               (ImmutableList.Builder<InitialCarState> lBldr, EmulatorProperties.InitialCarState car) -> {
                  lBldr.add(InitialCarState.build(cbldr -> {
                     cbldr.initialFloor(car.initialFloor)
                        .passengers(car.passengers.stream()
                        .<ImmutableList.Builder<PendingDropOff>> collect(
                           ImmutableList::<PendingDropOff> builder,
                           (ImmutableList.Builder<PendingDropOff> lBldr2, EmulatorProperties.PendingDropOff dropOff) -> {
                              lBldr2.add(PendingDropOff.build(pbldr -> {
                                 pbldr.id(idFactory.getNextPassengerId())
                                    .callTime(dropOff.callTime)
                                    .pickupTime(dropOff.pickupTime)
                                    .dropOffFloor(dropOff.dropOffFloor)
                                    .pickupFloor(dropOff.pickupFloor)
                                    .weight(dropOff.weight);
                              }));
                           },
                           (left, right) -> left.addAll(right.build())
                        ).build()
                     );
                  }));
               },
               (left, right) -> left.addAll(right.build())
            ).build()
         );
      });
   }

   @Override
   public DeploymentConfiguration hardenDeploymentConfig(DeploymentProperties mutable)
   {
      return DeploymentConfiguration.build(bldr -> {
         bldr.building(BuildingDescription.build(bbldr -> {
            bbldr.numFloors(mutable.building.numFloors)
               .numElevators(mutable.building.numElevators)
               .metersPerFloor(mutable.building.metersPerFloor);
         }))
            .topSpeed(TravelSpeedDescription.build(tbldr -> {
               tbldr.longAscent(mutable.topSpeed.longAscent)
               .longDescent(mutable.topSpeed.longDescent)
               .shortHop(mutable.topSpeed.shortHop);
            }))
            .motor(StartStopDescription.build(sbldr -> {
               sbldr.brakeDistance(mutable.motor.brakeDistance)
               .brakeSpeed(mutable.motor.brakeSpeed)
               .maxAcceleration(mutable.motor.maxAcceleration)
               .maxJerk(mutable.motor.maxJerk);
            }))
            .weight(WeightDescription.build(wbldr -> {
               wbldr.maxForTravel(mutable.weight.maxForTravel)
               .avgPassenger(mutable.weight.avgPassenger)
               .pctMaxForIdeal(mutable.weight.pctMaxForIdeal)
               .pctMaxForPickup(mutable.weight.pctMaxForPickup);
            }))
            .doors(DoorTimeDescription.build(dbldr -> {
               dbldr.minHold(mutable.doors.minHold)
               .personHold(mutable.doors.personHold)
               .openCloseTime(mutable.doors.openCloseTime);
            }));
      });
   }
}
