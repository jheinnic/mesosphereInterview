package test;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

public class TryGauss
{

   public static void main(String[] args)
   {
      Gaussian func = new Gaussian(1.0, 500, 100);
      for( int ii= 0; ii< 1000; ii+=25) {
         System.out.println(
            String.format("Gauss(%d) = %f", ii, func.value(ii)));
      }
      
      System.out.println(Float.MIN_NORMAL);
      System.out.println(Double.MIN_NORMAL);
      System.out.println(Integer.MAX_VALUE * Float.MIN_NORMAL);
      System.out.println(Long.MAX_VALUE * Float.MIN_NORMAL);

      func = new Gaussian(1.0, 500, Float.MIN_NORMAL);
      for( int ii= 0; ii< 1000; ii+=25) {
         System.out.println(
            String.format("Gauss(%d) = %f", ii, func.value(ii)));
      }
      
      StandardDeviation stdDev = new StandardDeviation();
      double[] samples = new double[] {50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0};
      System.out.println(stdDev.evaluate(samples));
   }

}
