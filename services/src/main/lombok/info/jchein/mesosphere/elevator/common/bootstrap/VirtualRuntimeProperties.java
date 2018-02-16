package info.jchein.mesosphere.elevator.common.bootstrap;


import javax.validation.constraints.Min;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;


@Data
@Validated
@ConfigurationProperties("mesosphere.runtime")
public class VirtualRuntimeProperties
{
   @Min(10)
   long tickDurationMillis;
}
