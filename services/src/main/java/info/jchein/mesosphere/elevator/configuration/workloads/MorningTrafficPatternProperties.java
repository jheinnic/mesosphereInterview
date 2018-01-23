package info.jchein.mesosphere.elevator.configuration.workloads;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties("mesosphere.workload.morning")
public class MorningTrafficPatternProperties {
	public static class DestinationPattern {
		/**
		 * The floor index destination an instance of this structure applies to
		 */
		public int floorIndex;
		
		/**
		 * Inter-arrival time for passengers to the given floor in milliseconds
		 */
		public double meanTime;
	}
	
	public DestinationPattern[] arrivals;
}
