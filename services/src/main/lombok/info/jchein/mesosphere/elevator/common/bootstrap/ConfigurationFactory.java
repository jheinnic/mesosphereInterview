package info.jchein.mesosphere.elevator.common.bootstrap;


import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.common.PassengerId;
import info.jchein.mesosphere.elevator.common.demographic.AgeGroupWeightSample;
import info.jchein.mesosphere.elevator.monitor.model.PendingDropOff;
import info.jchein.mesosphere.elevator.monitor.model.PendingPickup;


@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
class ConfigurationFactory
implements IConfigurationFactory
{
   private final Supplier<PassengerId> idFactory;


   @Autowired
   ConfigurationFactory( @NotNull Supplier<PassengerId> idFactory )
   {
      this.idFactory = idFactory;
   }


   @Override
   public EmulatorConfiguration hardenEmulatorConfig(EmulatorProperties mutable)
   {
      return EmulatorConfiguration.build(bldr -> {
         bldr.numFloors(mutable.getNumFloors())
            .driverAlias(mutable.getDriverAlias())
            .pendingPickups(mutable.getPendingPickups().stream().<ImmutableList.Builder<PendingPickup>> collect(
               ImmutableList::<PendingPickup> builder,
               (ImmutableList.Builder<PendingPickup> lBldr,
                  EmulatorProperties.PendingPickup pickup) -> {
                  lBldr.add(PendingPickup.build(pbldr -> {
                     pbldr.id(idFactory.get())
                        .callTime(pickup.callTime)
                        .dropOffFloor(pickup.dropOffFloor)
                        .pickupFloor(pickup.pickupFloor)
                        .weight(pickup.weight);
                  }));
               },
               (left, right) -> left.addAll(right.build()))
               .build())
            .cars(mutable.getCars().stream().<ImmutableList.Builder<InitialCarState>> collect(
               ImmutableList::<InitialCarState> builder,
               (ImmutableList.Builder<InitialCarState> lBldr,
                  EmulatorProperties.InitialCarState car) -> {
                  lBldr.add(InitialCarState.build(cbldr -> {
                     cbldr.initialFloor(car.initialFloor)
                        .passengers(car.passengers.stream().<ImmutableList
                        .Builder<PendingDropOff>> collect(
                           ImmutableList::<PendingDropOff> builder,
                           (ImmutableList.Builder<PendingDropOff> lBldr2,
                              EmulatorProperties.PendingDropOff dropOff) -> {
                              lBldr2.add(PendingDropOff.build(pbldr -> {
                                 pbldr.id(idFactory.get())
                                    .callTime(dropOff.callTime)
                                    .pickupTime(dropOff.pickupTime)
                                    .dropOffFloor(dropOff.dropOffFloor)
                                    .pickupFloor(dropOff.pickupFloor)
                                    .weight(dropOff.weight);
                              }));
                           },
                           (left, right) -> left.addAll(right.build()))
                           .build());
                  }));
               },
               (left, right) -> left.addAll(right.build()))
               .build());
      });
   }


   @Override
   public DeploymentConfiguration hardenDeploymentConfig(DeploymentProperties mutable)
   {
      return DeploymentConfiguration.build(bldr -> {
         bldr.building(BuildingDescription.build(bbldr -> {
            bbldr.numFloors(mutable.getBuilding().numFloors)
               .numElevators(mutable.getBuilding().numElevators)
               .metersPerFloor(mutable.getBuilding().metersPerFloor);
         }))
            .topSpeed(TravelSpeedDescription.build(tbldr -> {
               tbldr.longAscent(mutable.getTopSpeed().longAscent)
                  .longDescent(mutable.getTopSpeed().longDescent)
                  .shortHop(mutable.getTopSpeed().shortHop);
            }))
            .motor(StartStopDescription.build(sbldr -> {
               sbldr.brakeDistance(mutable.getMotor().brakeDistance)
                  .brakeSpeed(mutable.getMotor().brakeSpeed)
                  .maxAcceleration(mutable.getMotor().maxAcceleration)
                  .maxJerk(mutable.getMotor().maxJerk);
            }))
            .weight(WeightDescription.build(wbldr -> {
               wbldr.maxWeightAllowed(mutable.getWeight().maxForTravel)
                  .avgPassenger(mutable.getWeight().avgPassenger)
                  .pctMaxForIdeal(mutable.getWeight().pctMaxForIdeal)
                  .pctMaxForPickup(mutable.getWeight().pctMaxForPickup);
            }))
            .doors(DoorTimeDescription.build(dbldr -> {
               dbldr.minHold(mutable.getDoors().minHold)
                  .personHold(mutable.getDoors().personHold)
                  .openCloseTime(mutable.getDoors().openCloseTime);
            }));
      });
   }


   @Override
   public VirtualRuntimeConfiguration hardenVirtualRuntimeConfig(VirtualRuntimeProperties mutableProps)
   {
      return VirtualRuntimeConfiguration.build(bldr -> {
         bldr.tickDurationMillis(mutableProps.getTickDurationMillis());
      });
   }


   @Override
   public DemographicConfiguration hardenDemographicConfig(DemographicProperties mutableProps) {
      return DemographicConfiguration.build(bldr -> {
         bldr.femaleWeightSamples(
            mutableProps.getFemaleWeightSamples()
               .stream()
               .<AgeGroupWeightSample>map(sample -> {
		            return AgeGroupWeightSample.build(abldr -> {
		               abldr.minAge(sample.getAge().getMin())
		               .maxAge(sample.getAge().getMax())
		               .weightMean(sample.getWeight().getMean())
		               .weightStdDev(sample.getWeight().getStdDev())
		               .count(sample.getCount());
		            });
		         }).collect(Collectors.toList()))
         .maleWeightSamples(
            mutableProps.getMaleWeightSamples()
               .stream()
               .<AgeGroupWeightSample>map(sample -> {
                  return AgeGroupWeightSample.build(abldr -> {
                     abldr.minAge(sample.getAge().getMin())
                        .maxAge(sample.getAge().getMax())
                        .weightMean(sample.getWeight().getMean())
                        .weightStdDev(sample.getWeight().getStdDev())
                        .count(sample.getCount());
                 });
               }).collect(Collectors.toList()));
      });
   }
}
