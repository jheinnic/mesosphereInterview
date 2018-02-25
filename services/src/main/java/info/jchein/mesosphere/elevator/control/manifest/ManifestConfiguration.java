package info.jchein.mesosphere.elevator.control.manifest;

import javax.validation.constraints.NotNull;

import org.jgrapht.alg.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.common.CarIndexContext;
import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.elevator.common.bootstrap.DeploymentConfiguration;
import info.jchein.mesosphere.elevator.control.IElevatorCarScope;
import info.jchein.mesosphere.elevator.control.PerformanceEstimate;
import info.jchein.mesosphere.elevator.control.PickupImpactEstimate;
import info.jchein.mesosphere.elevator.control.manifest.bad.TravelGraphIndex;
import info.jchein.mesosphere.elevator.runtime.event.IRuntimeEventBus;

@Configuration
@ComponentScan
public class ManifestConfiguration
{
   private final DeploymentConfiguration deployConfig;
   
   @Autowired
   public ManifestConfiguration(DeploymentConfiguration deployConfig) {
      this.deployConfig = deployConfig;
   }
   
   @Bean
   @Scope(BeanDefinition.SCOPE_SINGLETON)
   TravelGraphFactory graphFactory() {
      return new TravelGraphFactory(this.deployConfig) {
         @Override
         protected TravelGraph allocateTravelGraph(TravelGraphIndex graphIndex)
         {
            return new TravelGraph(graphIndex);
         }
      };
   }
   
   @Bean
   @Autowired
   @Scope(IElevatorCarScope.SCOPE_NAME)
   PassengerManifest passengerManifest(
      @NotNull CarIndexContext carContext, @NotNull @Qualifier(IElevatorCarScope.SCOPE_NAME) IRuntimeEventBus eventBusLocal )
   {
      return new PassengerManifest(carContext, eventBusLocal) {
         @Override
         public PickupImpactEstimate estimatePickupImpact(int floorIndex, DirectionOfTravel direction,
            ImmutableList<Pair<Integer, Double>> passengers)
         {
            return null;
         }
         
         
         @Override
         public PerformanceEstimate estimatePerformance()
         {
            return null;
         }


         @Override
         protected ITravelGraph allocateTravelGraph(DirectionOfTravel direction)
         {
            return ManifestConfiguration.this.graphFactory().apply(direction);
         }
      };
   }
   
}
