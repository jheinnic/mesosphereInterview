package info.jchein.mesosphere.validator.constraint;

import java.util.UUID;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import info.jchein.mesosphere.validator.annotation.UUIDString;

public class UUIDStringValidator implements ConstraintValidator<UUIDString, String> {
	@Override
	public void initialize(UUIDString uuidString) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		if (value == null) {
			return true;
		}
		
		try {
			UUID uuid = UUID.fromString(value);
			return uuid != null;
		} catch( IllegalArgumentException e ) {
			// Not a UUID.
			return false;
		}
	}

}
