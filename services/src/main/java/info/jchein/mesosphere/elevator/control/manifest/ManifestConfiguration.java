package info.jchein.mesosphere.elevator.control.manifest;

import javax.validation.constraints.NotNull;

import org.jgrapht.alg.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
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
import info.jchein.mesosphere.elevator.runtime.temporal.IRuntimeClock;

@Configuration
@ComponentScan
public class ManifestConfiguration
{
   private final DeploymentConfiguration deployConfig;
   private final IRuntimeClock clock;
   
   @Autowired
   public ManifestConfiguration(DeploymentConfiguration deployConfig, IRuntimeClock clock) {
      this.deployConfig = deployConfig;
      this.clock = clock;
   }
   
   
   @Bean
   @Autowired
   @Scope(IElevatorCarScope.SCOPE_NAME)
   PassengerManifest passengerManifest( )
   {
      return new PassengerManifest(this.deployConfig.getBuilding().getNumFloors()) {
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


//         @Override
//         protected ITravelGraph allocateTravelGraph(DirectionOfTravel direction)
//         {
//            return ManifestConfiguration.this.graphFactory().apply(direction);
//         }
      };
   }
}
