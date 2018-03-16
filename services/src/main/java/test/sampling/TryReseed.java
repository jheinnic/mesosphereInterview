package test.sampling;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

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
      
      /*
      BigInteger iiFact = BigInteger.ONE;
      BigInteger iiPrev = BigInteger.ONE;
      System.out.println(
         String.format("%s %d", 
            iiFact.divide(BigInteger.ONE).toString(), 
            iiFact.divide(BigInteger.ONE).compareTo(iiPrev))
         ); 
      for( BigInteger iiBase = BigInteger.ONE; iiFact.divide(iiBase).compareTo(iiPrev) == 0; iiBase = iiBase.add(BigInteger.ONE), iiPrev = iiFact, iiFact = iiFact.multiply(iiBase)) {
         System.out.println(String.format("%s -> %s", iiBase.toString(), iiFact.toString()));
      }
      */
      
      final int n = 100;
      final int nMinusOne = n - 1;
      final ArrayList<Integer> source = new ArrayList<Integer>(100);
      int picks[] = new int[100];
      final Integer perm[] = new Integer[100];
      BigInteger iiFact = BigInteger.ONE;
      BigInteger iiBase = BigInteger.ZERO;

      for( int ii=0; ii<n; ii++ ) {
         iiBase = iiBase.add(BigInteger.ONE);
         iiFact = iiFact.multiply(iiBase);
      }
      System.out.println(iiFact.toString());
      final int rand = (int) ((Integer.MAX_VALUE - 1) * Math.random());
      BigInteger bigRand = BigInteger.valueOf(rand);
      BigInteger iiPermOne = iiFact.divide(bigRand.add(BigInteger.ONE)).multiply(bigRand);
      
      for (int jj=0; jj<10; jj++) {
         BigInteger iiPerm = iiPermOne.subtract(BigInteger.valueOf(jj));
	      for (int ii=1; ii<=n; ii++ ) {
	         BigInteger index = BigInteger.valueOf(ii);
	         BigInteger retVal = iiPerm.mod(index);
	         iiPerm = iiPerm.divide(index);
	         picks[ii-1] = retVal.intValueExact();
	         source.add(ii-1);
	      }

	      for (int ii=nMinusOne; ii>=0; ii--) {
	         perm[nMinusOne-ii] = source.remove(picks[ii]);
	      }
	      System.out.println(Arrays.toString(perm));
      }
//      BigInteger iiFact = BigInteger.ONE;
//      for( long ii = 1; iiFact.compareTo(BigInteger.ZERO) > 0; ii++, iiFact = iiFact.multiply(BigInteger.valueOf(ii)) ) {
//         System.out.println(String.format("%d -> %s", ii, iiFact.toString()));
//      }
   }

}
