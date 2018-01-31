package info.jchein.mesosphere.elevator.domain.model;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import info.jchein.mesosphere.elevator.domain.event.ElevatorCarEvent;

public class PollableEventQueue
{
   private final EventBus eventBus;
   private final LinkedBlockingQueue<ElevatorCarEvent> eventQueue;

   PollableEventQueue(EventBus eventBus) {
      this.eventBus = eventBus;
      this.eventQueue = new LinkedBlockingQueue<ElevatorCarEvent>();
   }
   
   @PostConstruct
   public void init() {
      this.eventBus.register(this);
   }
   
   @Subscribe
   public void enqueueEvent( ElevatorCarEvent event ) {
      this.eventQueue.offer(event);
   }
   
   public void pollQueue(Collection<ElevatorCarEvent> eventSink) {
      this.eventQueue.drainTo(eventSink);
   }
}
