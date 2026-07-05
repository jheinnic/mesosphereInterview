package info.jchein.mesosphere.validator.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import info.jchein.mesosphere.elevator.common.DirectionOfTravel;
import info.jchein.mesosphere.validator.annotation.Moving;

/**
 * Validate that the character sequence (e.g. string) is a valid URL using a
 * regular expression.
 *
 * @author Hardy Ferentschik
 *
 * @since 5.2
 */
public class MovingValidator implements ConstraintValidator<Moving, DirectionOfTravel> {
	@Override
	public void initialize(Moving moving) {
	}

	@Override
	public boolean isValid(DirectionOfTravel value, ConstraintValidatorContext constraintValidatorContext) {
		if ((value == null) || (value == DirectionOfTravel.STOPPED)) {
			return false;
		}
		
		return (value == DirectionOfTravel.GOING_DOWN) || (value == DirectionOfTravel.GOING_UP);
	}
}
