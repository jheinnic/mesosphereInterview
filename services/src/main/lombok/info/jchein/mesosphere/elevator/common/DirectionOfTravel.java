package info.jchein.mesosphere.elevator.common;

import java.util.Comparator;

public enum DirectionOfTravel implements Comparator<Integer> {
	STOPPED,
	GOING_UP,
	GOING_DOWN;
   
   public int floorOrder(int fromFloor, int toFloor) {
      if (fromFloor == toFloor) {
         return 0;
      } else if (this == STOPPED) {
         throw new IllegalStateException("Non-alike floor ordering is undefined when not travelling in either direction");
      } else if (this == GOING_UP) {
         if (fromFloor < toFloor) {
            return -1;
         } else {
            return 1;
         }
      } else if (toFloor < fromFloor) {
         return -1;
      } else {
         return 1;
      }
   }

   @Override
   public int compare(Integer o1, Integer o2)
   {
      return this.floorOrder(o1, o2);
   }
}
