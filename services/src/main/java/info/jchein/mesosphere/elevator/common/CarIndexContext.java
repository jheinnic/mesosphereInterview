package info.jchein.mesosphere.elevator.common;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;


//@Component
//@Scope(BeanDefinition.SCOPE_SINGLETON)
public class CarIndexContext implements ICarContext
{
   private static Interner<CarIndex> internCache = Interners.newStrongInterner();
   private ThreadLocal<CarIndex> carContext = new ThreadLocal<CarIndex>();;

   @Override
   public CarIndex get()
   {
      return this.carContext.get();
   }
   
   public int getCarIndex() {
      return this.carContext.get().getCarIndex();
   }

   @Override
   public void accept(CarIndex t)
   {
      this.carContext.set(
         (t != null) ? CarIndexContext.internCache.intern(t) : t);
   }
   
   public void setCarIndex(int carIndex) {
      final CarIndex t = CarIndex.of(carIndex);
      this.carContext.set(
         CarIndexContext.internCache.intern(t));
   }

   @Override
   public CarIndex apply(CarIndex t)
   {
      final CarIndex retVal = this.carContext.get();
      this.carContext.set(
         (t != null) ? CarIndexContext.internCache.intern(t) : t);
      return retVal;
   }

   @Override
   public CarIndex swapCarIndex(int carIndex)
   {
      final CarIndex retVal = 
         CarIndexContext.internCache.intern(
             CarIndex.of(carIndex)
         );
      this.carContext.set(retVal);

      return retVal;
   }
}
