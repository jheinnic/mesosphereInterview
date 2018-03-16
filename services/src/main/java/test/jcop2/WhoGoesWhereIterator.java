package test.jcop2;

import java.util.NoSuchElementException;

import cz.cvut.felk.cig.jcop.problem.Configuration;
import cz.cvut.felk.cig.jcop.problem.Operation;
import cz.cvut.felk.cig.jcop.problem.OperationIterator;

public class WhoGoesWhereIterator implements OperationIterator
{
   public WhoGoesWhereIterator( Configuration configuration, WhoGoesWhere whoGoesWhere )
   {
   }

   @Override
   public Operation getRandomOperation() throws UnsupportedOperationException
   {
      throw new UnsupportedOperationException("getRandomOperation");
   }

   @Override
   public boolean hasNext()
   {
      return false;
   }

   @Override
   public Operation next()
   {
      throw new NoSuchElementException();
   }

   @Override
   public void remove()
   {
      throw new UnsupportedOperationException("remove");
   }
}
