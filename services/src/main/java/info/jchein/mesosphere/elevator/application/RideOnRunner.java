package info.jchein.mesosphere.elevator.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import info.jchein.mesosphere.elevator.runtime.IRuntimeClock;
import info.jchein.mesosphere.elevator.runtime.IRuntimeScheduler;
import info.jchein.mesosphere.elevator.runtime.virtual.RuntimeClock;

@Component
public class RideOnRunner
implements ApplicationRunner
{
   @Autowired
   public RideOnRunner( IRuntimeScheduler scheduler, IRuntimeClock clock ) {
      
   }
   
   
   @Override
   public void run(ApplicationArguments args) throws Exception
   {
     

   }

}
