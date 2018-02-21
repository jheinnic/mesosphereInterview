package info.jchein.mesosphere.elevator.control.event;

import org.jctools.queues.atomic.SpscLinkedAtomicQueue;

import com.google.common.eventbus.Subscribe;

import info.jchein.mesosphere.elevator.runtime.event.PollableEventQueue;

public class ElevatorCarEventQueue extends PollableEventQueue<ElevatorCarEvent>
{
   public ElevatorCarEventQueue( SpscLinkedAtomicQueue<ElevatorCarEvent> eventQueue )
   {
      super(eventQueue);
   }

   @Subscribe
   public void onElevatorEvent(ElevatorCarEvent event) {
      this.offer(event);
   }
}
