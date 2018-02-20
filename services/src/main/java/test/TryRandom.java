package test;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.ValueServer;

import lombok.SneakyThrows;

public class TryRandom
{
   @SneakyThrows
   public static void main(String[] args) {
      int[] seed = { 99583, 475820, -4, 862278, -923873, 0, 1837, 816793, -392, -84292442, 2928035, 4253};
      RandomGenerator rg = new MersenneTwister(seed);
      ValueServer vs = new ValueServer(rg);
      vs.setMu(75.6);
      double mu = vs.getMu();

      RandomDataGenerator rdg = new RandomDataGenerator(rg);
      ExponentialDistribution expDist = new ExponentialDistribution(rg, mu);
      
      System.out.println("Exponential Value Server");
      vs.setMode(ValueServer.EXPONENTIAL_MODE);
      for (int ii=0; ii<30; ii++) {
         System.out.println(String.format( "#%d: %f", ii+1, vs.getNext()));
      }

      System.out.println("RandomData Poission");
      for (int ii=0; ii<30; ii++) {
         System.out.println(String.format( "#%d: %d", ii+1, rdg.nextPoisson(mu)));
      }

      System.out.println("RandomData Exponential");
      for (int ii=0; ii<30; ii++) {
         System.out.println(String.format( "#%d: %f", ii+1, rdg.nextExponential(mu)));
      }

      System.out.println("ExponentialDistribution Samples");
      for (int ii=0; ii<30; ii++) {
         System.out.println(String.format( "#%d: %f", ii+1, expDist.sample()));
      }
      
      System.out.println("Gaussian Value Server");
      vs.setMode(ValueServer.GAUSSIAN_MODE);
      vs.setMu(78.8);
      vs.setSigma(12.9);
      for (int ii=0; ii<30; ii++) {
         System.out.println(String.format( "#%d: %f", ii+1, vs.getNext()));
      }

   }
}
