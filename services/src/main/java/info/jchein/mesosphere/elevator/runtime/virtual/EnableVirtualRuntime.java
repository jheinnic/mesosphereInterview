package info.jchein.mesosphere.elevator.runtime.virtual;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(VirtualRuntimeConfiguration.class)
@ActiveProfiles("elevator.runtime.virtual")
public @interface EnableVirtualRuntime
{

}
