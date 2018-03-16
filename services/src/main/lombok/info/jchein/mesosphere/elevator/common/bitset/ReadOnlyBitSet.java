package info.jchein.mesosphere.elevator.common.bitset;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.Arrays;


public class ReadOnlyBitSet
extends AbstractBitSet
implements IReadOnlyBitSet
{
   /**
    * Creates a new bit set. All bits are {@code false}.
    */
   public ReadOnlyBitSet()
   {
      super();
   }


   public ReadOnlyBitSet( long[] copyOf )
   {
      super(copyOf);
   }


   /**
    * Creates a bit set whose initial size is large enough to explicitly represent bits with indices in the range
    * {@code 0} through {@code nbits-1}. All bits are initially {@code false}.
    *
    * @param nbits
    *           the initial size of the bit set
    * @throws NegativeArraySizeException
    *            if the specified initial size is negative
    */
   ReadOnlyBitSet( int nbits )
   {
      super(nbits);
   }


   /**
    * Returns a new bit set containing all the bits in the given long array.
    *
    * <p>
    * 
    * More precisely, <br>
    * {@code BitSet.valueOf(longs).get(n) == ((longs[n/64] & (1L<<(n%64))) != 0)} <br>
    * for all {@code n < 64 * longs.length}.
    *
    * <p>
    * This method is equivalent to {@code BitSet.valueOf(LongBuffer.wrap(longs))}.
    *
    * @param longs
    *           a long array containing a little-endian representation of a sequence of bits to be used as the initial
    *           bits of the new bit set
    * @return a {@code BitSet} containing all the bits in the long array
    * @since 1.7
    */
   public static ReadOnlyBitSet valueOf(long[] longs)
   {
      int n;
      for (n = longs.length; n > 0 && longs[n - 1] == 0; n--)
         ;
      return new ReadOnlyBitSet(Arrays.copyOf(longs, n));
   }


   /**
    * Returns a new bit set containing all the bits in the given long buffer between its position and limit.
    *
    * <p>
    * More precisely, <br>
    * {@code ReadOnlyBitSet.valueOf(lb).get(n) == ((lb.get(lb.position()+n/64) & (1L<<(n%64))) != 0)} <br>
    * for all {@code n < 64 * lb.remaining()}.
    *
    * <p>
    * The long buffer is not modified by this method, and no reference to the buffer is retained by the bit set.
    *
    * @param lb
    *           a long buffer containing a little-endian representation of a sequence of bits between its position and
    *           limit, to be used as the initial bits of the new bit set
    * @return a {@code ReadOnlyBitSet} containing all the bits in the buffer in the specified range
    * @since 1.7
    */
   public static ReadOnlyBitSet valueOf(LongBuffer lb)
   {
      lb = lb.slice();
      int n;
      for (n = lb.remaining(); n > 0 && lb.get(n - 1) == 0; n--)
         ;
      long[] words = new long[n];
      lb.get(words);
      return new ReadOnlyBitSet(words);
   }


   /**
    * Returns a new bit set containing all the bits in the given byte array.
    *
    * <p>
    * More precisely, <br>
    * {@code ReadOnlyBitSet.valueOf(bytes).get(n) == ((bytes[n/8] & (1<<(n%8))) != 0)} <br>
    * for all {@code n <  8 * bytes.length}.
    *
    * <p>
    * This method is equivalent to {@code ReadOnlyBitSet.valueOf(ByteBuffer.wrap(bytes))}.
    *
    * @param bytes
    *           a byte array containing a little-endian representation of a sequence of bits to be used as the initial
    *           bits of the new bit set
    * @return a {@code ReadOnlyBitSet} containing all the bits in the byte array
    * @since 1.7
    */
   public static ReadOnlyBitSet valueOf(byte[] bytes)
   {
      return ReadOnlyBitSet.valueOf(ByteBuffer.wrap(bytes));
   }


   /**
    * Returns a new bit set containing all the bits in the given byte buffer between its position and limit.
    *
    * <p>
    * More precisely, <br>
    * {@code ReadOnlyBitSet.valueOf(bb).get(n) == ((bb.get(bb.position()+n/8) & (1<<(n%8))) != 0)} <br>
    * for all {@code n < 8 * bb.remaining()}.
    *
    * <p>
    * The byte buffer is not modified by this method, and no reference to the buffer is retained by the bit set.
    *
    * @param bb
    *           a byte buffer containing a little-endian representation of a sequence of bits between its position and
    *           limit, to be used as the initial bits of the new bit set
    * @return a {@code ReadOnlyBitSet} containing all the bits in the buffer in the specified range
    * @since 1.7
    */
   public static ReadOnlyBitSet valueOf(ByteBuffer bb)
   {
      bb =
         bb.slice()
            .order(ByteOrder.LITTLE_ENDIAN);
      int n;
      for (n = bb.remaining(); n > 0 && bb.get(n - 1) == 0; n--)
         ;
      long[] words = new long[(n + 7) / 8];
      bb.limit(n);
      int i = 0;
      while (bb.remaining() >= 8)
         words[i++] = bb.getLong();
      for (int remaining = bb.remaining(), j = 0; j < remaining; j++)
         words[i] |= (bb.get() & 0xffL) << (8 * j);
      return new ReadOnlyBitSet(words);
   }


   /**
    * Cloning this {@code BitSet} produces a new {@code BitSet} that is equal to it. The clone of the bit set is another
    * bit set that has exactly the same bits set to {@code true} as this bit set.
    *
    * @return a clone of this bit set
    * @see #size()
    */
   public Object clone()
   {
      try {
         ReadOnlyBitSet result = (ReadOnlyBitSet) super.clone();
         result.words = words.clone();
         result.checkInvariants();
         return result;
      }
      catch (CloneNotSupportedException e) {
         throw new InternalError(e);
      }
   }


   // Bad heuristic. Sometimes this may be called when Java identitity of the returned object is expected to be
   // different. The heuristic reasoning
   // is available when calling asReadOnly() instead.
   // public IReadOnlyBitSet readOnlyCopy()
   // {
   // return this;
   // }

   @Override
   public boolean isReadOnly()
   {
      return true;
   }


   @Override
   public IReadOnlyBitSet asReadOnly()
   {
      return this;
   }


   /**
    * Save the state of the {@code ReadOnlyBitSet} instance to a stream (i.e., serialize it).
    */
   private void writeObject(ObjectOutputStream s) throws IOException
   {

      checkInvariants();

      ObjectOutputStream.PutField fields = s.putFields();
      fields.put("bits", words);
      s.writeFields();
   }


   /**
    * Reconstitute the {@code ReadOnlyBitSet} instance from a stream (i.e., deserialize it).
    */
   private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException
   {

      ObjectInputStream.GetField fields = s.readFields();
      words = (long[]) fields.get("bits", null);

      // Assume maximum length then find real length
      // because recalculateWordsInUse assumes maintenance
      // or reduction in logical size
      wordsInUse = words.length;
      recalculateWordsInUse();
      checkInvariants();
   }

}
