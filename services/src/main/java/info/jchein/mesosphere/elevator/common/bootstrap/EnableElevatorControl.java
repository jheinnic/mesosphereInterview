package info.jchein.mesosphere.elevator.control.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

import info.jchein.mesosphere.elevator.common.bootstrap.EmulatorProperties;
import info.jchein.mesosphere.elevator.runtime.virtual.EnableVirtualRuntime;

@EnableConfigurationProperties(EmulatorProperties.class)
@Import(ElevatorControllerConfiguration.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableElevatorController
{

}
