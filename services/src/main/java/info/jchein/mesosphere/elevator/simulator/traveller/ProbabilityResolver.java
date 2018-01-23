package info.jchein.mesosphere.elevator.simulator.traveller;

import java.util.Arrays;

import org.hibernate.validator.constraints.Range;

public class ProbabilityResolver<T> implements IProbabilityResolver<T> {
	final double[] cdfLimits;
	final T[] mappedValues;
	
	public ProbabilityResolver(double[] cdfLimits, T[] mappedValues) {
		this.cdfLimits = cdfLimits;
		this.mappedValues = mappedValues;
	}

	@Override
	public T resolve( @Range(min=0, max=1) double randomValue) {
		final int index = Arrays.binarySearch(this.cdfLimits, randomValue);
		if (index < 0 || index >= this.mappedValues.length) {
			throw new IllegalArgumentException( String.format("%f is outside the cdf range", randomValue));
		}
		
		return this.mappedValues[index];
	}

}
