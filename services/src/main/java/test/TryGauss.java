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
   }

}
