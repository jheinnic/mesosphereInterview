package info.jchein.mesosphere.elevator.emulator.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import info.jchein.mesosphere.elevator.control.model.EnableElevatorController;
import info.jchein.mesosphere.elevator.runtime.virtual.EnableVirtualRuntime;

@EnableElevatorController
@Import(EmulatedElevatorDriverConfiguration.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableEmulatedElevator
{

}
