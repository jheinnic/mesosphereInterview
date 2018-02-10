package info.jchein.mesosphere.elevator.simulator.passengers;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableList;

import info.jchein.mesosphere.elevator.control.event.TravellerSourceNames;
import info.jchein.mesosphere.elevator.control.event.TravellerSourceNames.TravellerSourceNamesBuilder;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class TravellerSourceNames
{
   @Singular
   ImmutableList<String> sourceNames; 
   
   public static TravellerSourceNames build(Consumer<TravellerSourceNamesBuilder> director)
   {
      final TravellerSourceNamesBuilder bldr = TravellerSourceNames.builder();
      director.accept(bldr);
      return bldr.build();
   }


   public TravellerSourceNames copy(Consumer<TravellerSourceNamesBuilder> director)
   {
      final TravellerSourceNamesBuilder bldr = this.toBuilder();
      director.accept(bldr);
      return bldr.build();
   }
}
