package info.jchein.mesosphere.elevator.emulator.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix="mesosphere.elevator.emulator")
public class EmulatorRegistrationConfigurationProperties
{
   /**
    * The emulator registers its driver factory with a lookup key of "emulator" by default, but since shorthand names can potentially yield
    * conflicts with other extensions that want to use the same name, this configuration property is available to override the default lookup
    * key.
    * 
    * If setting this property, make sure to use the same string in <code>mesosphere.elevator.control.carDriver</cod> to request the Emulator driver
    * by lookup key.
    */
   String driverAlias = "emulator";
}
