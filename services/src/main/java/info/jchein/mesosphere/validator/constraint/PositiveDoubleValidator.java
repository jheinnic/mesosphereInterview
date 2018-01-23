package info.jchein.mesosphere.validator.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import info.jchein.mesosphere.validator.annotation.Positive;

/**
 * Validate that the character sequence (e.g. string) is a valid URL using a
 * regular expression.
 *
 * @author Hardy Ferentschik
 *
 * @since 5.2
 */
public class PositiveDoubleValidator implements ConstraintValidator<Positive, Double> {
	@Override
	public void initialize(Positive positive) {
	}

	@Override
	public boolean isValid(Double value, ConstraintValidatorContext constraintValidatorContext) {
		if (value == null) {
			return true;
		}
		
		return value.doubleValue() > 0;
	}
}
