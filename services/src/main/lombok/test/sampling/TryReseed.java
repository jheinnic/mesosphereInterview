package test.sampling;

import java.math.BigInteger;
import java.util.HashSet;
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
      final HashSet<Integer> source = new HashSet<Integer>(100);
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
      System.out.println(iiPermOne.toString());
      
      for (int jj=0; jj<10; jj++) {
         BigInteger iiPerm = iiPermOne.subtract(BigInteger.valueOf(jj));
         System.out.println(String.format("jj=%d", jj));
         System.out.println(iiPerm.toString());
	      for (int ii=1; ii<=n; ii++ ) {
	         BigInteger index = BigInteger.valueOf(ii);
	         BigInteger retVal = iiPerm.mod(index);
	         iiPerm = iiPerm.divide(index);
	         picks[ii-1] = retVal.intValueExact();
            // System.out.println(String.format("ii=%d", ii));
            // System.out.println(retVal.toString());
	         source.add(ii-1);
            // System.out.println(source);
	      }

	      for (int ii=nMinusOne; ii>=0; ii--) {
            // System.out.println(String.format("ii=%d, n-ii=%d, picks[ii]=%d, src=d", ii, nMinusOne-ii, picks[ii])); //  source.has(picks[ii])));
            // System.out.println(source);
            perm[nMinusOne-ii] = source.remove(picks[ii]) ? picks[ii] : -1;
            // System.out.println(source);

	      }
	      System.out.println(Arrays.toString(perm));
      }
//      BigInteger iiFact = BigInteger.ONE;
//      for( long ii = 1; iiFact.compareTo(BigInteger.ZERO) > 0; ii++, iiFact = iiFact.multiply(BigInteger.valueOf(ii)) ) {
//         System.out.println(String.format("%d -> %s", ii, iiFact.toString()));
//      }
   }
}
