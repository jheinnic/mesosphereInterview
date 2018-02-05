package info.jchein.mesosphere.elevator.common;

import java.util.function.Consumer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder=true)
class TravelItineraryItem {
    private final int floorIndex;
    private final DirectionOfTravel travelDirection;
    private final boolean forPickup;
    private final boolean forDropOff;
    

    public static TravelItineraryItem build(Consumer<TravelItineraryItemBuilder> director) {
       TravelItineraryItemBuilder bldr = TravelItineraryItem.builder();
       director.accept(bldr);
       return bldr.build();
    }
    
    public TravelItineraryItem copy(Consumer<TravelItineraryItemBuilder> director) {
       TravelItineraryItemBuilder bldr = this.toBuilder();
       director.accept(bldr);
       return bldr.build();
    }
}
