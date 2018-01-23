package info.jchein.mesosphere.elevator.simulator.traveller;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;


@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@NotNull
@Pattern(regexp="^([a-zA-Z]*:[0-9]+(\\.[0-9]+)?;)*[a-zA-Z]*:[0-9]+(\\.[0-9]+)?$")
public @interface ActivityCdfDef {

}
