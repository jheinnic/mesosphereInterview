package info.jchein.mesosphere.elevator.common.bootstrap;

import lombok.Data;

@Data
public class PendingDropoff
{
   public long callTime;
   public long pickupTime;
   public int pickupFloor;
   public int dropoffFloor;
   public double weight;
}
