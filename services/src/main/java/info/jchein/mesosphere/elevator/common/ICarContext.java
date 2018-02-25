package info.jchein.mesosphere.elevator.common;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ICarContext extends Supplier<CarIndex>, Consumer<CarIndex>, Function<CarIndex, CarIndex>
{
   public int getCarIndex();
   public void setCarIndex(int carIndex);
   public CarIndex swapCarIndex(int carIndex);
}
