package info.jchein.mesosphere.domain.clock;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemClockTick {
   private final long clockTime;
}
