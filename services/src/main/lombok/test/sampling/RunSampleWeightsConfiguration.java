package test.sampling;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import info.jchein.mesosphere.elevator.common.IdentityConfiguration;
import info.jchein.mesosphere.elevator.common.bootstrap.BootstrapConfiguration;
import info.jchein.mesosphere.elevator.common.probability.ProbabilityUtilsConfiguration;
import test.jcop2.WhoGoesWhereConfiguration;

@Configuration
@Import({
   RunMatchRidersRunner.class, IdentityConfiguration.class, BootstrapConfiguration.class,
   ProbabilityUtilsConfiguration.class, WhoGoesWhereConfiguration.class
})
public class RunSampleWeightsConfiguration {
   RunSampleWeightsConfiguration() { }
}