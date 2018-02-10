package info.jchein.mesosphere.elevator.common.bootstrap;

import java.util.LinkedList;

import lombok.Data;
import lombok.Singular;

@Data
public class InitialCarState
{
   public int initialFloor;
   
   @Singular
   public final LinkedList<PendingDropoff> passengers = new LinkedList<PendingDropoff>();
}
