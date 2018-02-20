package info.jchein.mesosphere.elevator.common.bootstrap;

public interface IConfigurationFactory
{
   EmulatorConfiguration hardenEmulatorConfig( EmulatorProperties mutableProps );

   DeploymentConfiguration hardenDeploymentConfig( DeploymentProperties mutableProps );
   
   VirtualRuntimeDescription hardenVirtualRuntimeConfig( VirtualRuntimeProperties mutableProps );

   DemographicConfiguration hardenDemographicConfig(DemographicProperties mutableProps);
}
