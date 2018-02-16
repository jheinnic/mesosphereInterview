package info.jchein.mesosphere.elevator.runtime;

import java.util.concurrent.TimeUnit;

import rx.functions.Action0;

public interface IRuntimeScheduler
{
   public static final int HI_PRIORITY = Integer.MIN_VALUE;
   public static final int LO_PRIORITY = Integer.MAX_VALUE;
   public static final int DEFAULT_PRIORITY = 0;

   void scheduleOnce(Action0 action, long interval, TimeUnit intervalUnit);
   
   void scheduleOnce(long interval, TimeUnit intervalUnit, int priority, IIntervalHandler handler);

   void scheduleInterrupt(long cycleInterval, TimeUnit intervalUnit, int priority, IIntervalHandler handler);
   
   void scheduleVariable(long firstInterval, TimeUnit intervalUnit, int priority, IVariableIntervalFunction handler);

}
