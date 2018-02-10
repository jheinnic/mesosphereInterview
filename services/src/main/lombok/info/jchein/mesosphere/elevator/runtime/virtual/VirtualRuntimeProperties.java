package info.jchein.mesosphere.elevator.runtime.virtual;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties
public class VirtualRuntimeProperties
{
    long tickDurationMillis;
}
