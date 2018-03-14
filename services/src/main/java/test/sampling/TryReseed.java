package test.sampling;

import cz.cvut.felk.cig.jcop.util.JcopRandom;

public class TryReseed
{

   public static void main(String[] args)
   {
      // TODO Auto-generated method stub
      JcopRandom.setSeed(123456789);
      System.out.println(
         String.format("%f %f %f", JcopRandom.nextDouble(), JcopRandom.nextDouble(), JcopRandom.nextDouble()));
      System.out.println(
         String.format("%f %f %f", JcopRandom.nextDouble(), JcopRandom.nextDouble(), JcopRandom.nextDouble()));
      JcopRandom.setSeed(123456789);
      System.out.println(
         String.format("%f %f %f", JcopRandom.nextDouble(), JcopRandom.nextDouble(), JcopRandom.nextDouble()));
      JcopRandom.setSeed(987654321);
      System.out.println(
         String.format("%f %f %f", JcopRandom.nextDouble(), JcopRandom.nextDouble(), JcopRandom.nextDouble()));
      System.out.println(
         String.format("%f %f %f", JcopRandom.nextDouble(), JcopRandom.nextDouble(), JcopRandom.nextDouble()));
      JcopRandom.setSeed(987654321);
      System.out.println(
         String.format("%f %f %f", JcopRandom.nextDouble(), JcopRandom.nextDouble(), JcopRandom.nextDouble()));
      JcopRandom.setSeed(123456789);
      System.out.println(
         String.format("%f %f %f", JcopRandom.nextDouble(), JcopRandom.nextDouble(), JcopRandom.nextDouble()));
      System.out.println(
         String.format("%f %f %f", JcopRandom.nextDouble(), JcopRandom.nextDouble(), JcopRandom.nextDouble()));
   }

}
