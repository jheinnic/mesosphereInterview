package info.jchein.mesosphere.elevator.common.bitset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.Arrays;

public class MutableBitSet
extends AbstractBitSet
implements IMutableBitSet
{
   private static final long serialVersionUID = 2334412973090077284L;
   /**
    * Whether the size of "words" is user-specified. If so, we assume the user knows what he's doing and try harder to
    * preserve it.
    */
   protected transient boolean sizeIsSticky = false;
   
   /**
    * Creates a new bit set. All bits are initially {@code false}.
    */
   public MutableBitSet() {
      super();
      sizeIsSticky = false;
   }


   public MutableBitSet( long[] copyOf )
   {
      super(copyOf);
      sizeIsSticky = false;
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
   public MutableBitSet( int nbits ) {
      super(nbits);
      sizeIsSticky = true;
   }


   /**
    * Returns a new bit set containing all the bits in the given long array.
    *
    * <p>
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
   public static MutableBitSet valueOf(long[] longs)
   {
      int n;
      for (n = longs.length; n > 0 && longs[n - 1] == 0; n--)
         ;
      return new MutableBitSet(Arrays.copyOf(longs, n));
   }


   /**
    * Returns a new bit set containing all the bits in the given long buffer between its position and limit.
    *
    * <p>
    * More precisely, <br>
    * {@code ModifiableBitSet.valueOf(lb).get(n) == ((lb.get(lb.position()+n/64) & (1L<<(n%64))) != 0)} <br>
    * for all {@code n < 64 * lb.remaining()}.
    *
    * <p>
    * The long buffer is not modified by this method, and no reference to the buffer is retained by the bit set.
    *
    * @param lb
    *           a long buffer containing a little-endian representation of a sequence of bits between its position and
    *           limit, to be used as the initial bits of the new bit set
    * @return a {@code ModifiableBitSet} containing all the bits in the buffer in the specified range
    * @since 1.7
    */
   public static MutableBitSet valueOf(LongBuffer lb)
   {
      lb = lb.slice();
      int n;
      for (n = lb.remaining(); n > 0 && lb.get(n - 1) == 0; n--)
         ;
      long[] words = new long[n];
      lb.get(words);
      return new MutableBitSet(words);
   }


   /**
    * Returns a new bit set containing all the bits in the given byte array.
    *
    * <p>
    * More precisely, <br>
    * {@code ModifiableBitSet.valueOf(bytes).get(n) == ((bytes[n/8] & (1<<(n%8))) != 0)} <br>
    * for all {@code n <  8 * bytes.length}.
    *
    * <p>
    * This method is equivalent to {@code ModifiableBitSet.valueOf(ByteBuffer.wrap(bytes))}.
    *
    * @param bytes
    *           a byte array containing a little-endian representation of a sequence of bits to be used as the initial
    *           bits of the new bit set
    * @return a {@code ModifiableBitSet} containing all the bits in the byte array
    * @since 1.7
    */
   public static MutableBitSet valueOf(byte[] bytes)
   {
      return MutableBitSet.valueOf(ByteBuffer.wrap(bytes));
   }


   /**
    * Returns a new bit set containing all the bits in the given byte buffer between its position and limit.
    *
    * <p>
    * More precisely, <br>
    * {@code ModifiableBitSet.valueOf(bb).get(n) == ((bb.get(bb.position()+n/8) & (1<<(n%8))) != 0)} <br>
    * for all {@code n < 8 * bb.remaining()}.
    *
    * <p>
    * The byte buffer is not modified by this method, and no reference to the buffer is retained by the bit set.
    *
    * @param bb
    *           a byte buffer containing a little-endian representation of a sequence of bits between its position and
    *           limit, to be used as the initial bits of the new bit set
    * @return a {@code ModifiableBitSet} containing all the bits in the buffer in the specified range
    * @since 1.7
    */
   public static MutableBitSet valueOf(ByteBuffer bb)
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
      return new MutableBitSet(words);
   }

   /**
    * Ensures that the BitSet can hold enough words.
    * 
    * @param wordsRequired
    *           the minimum acceptable number of words.
    */
   private void ensureCapacity(int wordsRequired)
   {
      if (words.length < wordsRequired) {
         // Allocate larger of doubled size or required size
         int request = Math.max(2 * words.length, wordsRequired);
         words = Arrays.copyOf(words, request);
         sizeIsSticky = false;
      }
   }

   /**
    * Ensures that the BitSet can accommodate a given wordIndex, temporarily violating the invariants. The caller must
    * restore the invariants before returning to the user, possibly using recalculateWordsInUse().
    * 
    * @param wordIndex
    *           the index to be accommodated.
    */
   private void expandTo(int wordIndex)
   {
      int wordsRequired = wordIndex + 1;
      if (wordsInUse < wordsRequired) {
         ensureCapacity(wordsRequired);
         wordsInUse = wordsRequired;
      }
   }

   /**
    * Sets the bit at the specified index to the complement of its current value.
    *
    * @param bitIndex
    *           the index of the bit to flip
    * @throws IndexOutOfBoundsException
    *            if the specified index is negative
    * @since 1.4
    */
   public MutableBitSet flip(int bitIndex)
   {
      if (bitIndex < 0) throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
   
      int wordIndex = wordIndex(bitIndex);
      expandTo(wordIndex);
   
      words[wordIndex] ^= (1L << bitIndex);
   
      recalculateWordsInUse();
      checkInvariants();
      
      return this;
   }

   /**
    * Sets each bit from the specified {@code fromIndex} (inclusive) to the specified {@code toIndex} (exclusive) to the
    * complement of its current value.
    *
    * @param fromIndex
    *           index of the first bit to flip
    * @param toIndex
    *           index after the last bit to flip
    * @throws IndexOutOfBoundsException
    *            if {@code fromIndex} is negative, or {@code toIndex} is negative, or {@code fromIndex} is larger than
    *            {@code toIndex}
    * @since 1.4
    */
   public MutableBitSet flip(int fromIndex, int toIndex)
   {
      checkRange(fromIndex, toIndex);
   
      if (fromIndex == toIndex) return this;
   
      int startWordIndex = wordIndex(fromIndex);
      int endWordIndex = wordIndex(toIndex - 1);
      expandTo(endWordIndex);
   
      long firstWordMask = WORD_MASK << fromIndex;
      long lastWordMask = WORD_MASK >>> -toIndex;
      if (startWordIndex == endWordIndex) {
         // Case 1: One word
         words[startWordIndex] ^= (firstWordMask & lastWordMask);
      } else {
         // Case 2: Multiple words
         // Handle first word
         words[startWordIndex] ^= firstWordMask;
   
         // Handle intermediate words, if any
         for (int i = startWordIndex + 1; i < endWordIndex; i++)
            words[i] ^= WORD_MASK;
   
         // Handle last word
         words[endWordIndex] ^= lastWordMask;
      }
   
      recalculateWordsInUse();
      checkInvariants();
      
      return this;
   }

   /**
    * Sets the bit at the specified index to {@code true}.
    *
    * @param bitIndex
    *           a bit index
    * @throws IndexOutOfBoundsException
    *            if the specified index is negative
    * @since JDK1.0
    */
   public MutableBitSet set(int bitIndex)
   {
      if (bitIndex < 0) throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
   
      int wordIndex = wordIndex(bitIndex);
      expandTo(wordIndex);
   
      words[wordIndex] |= (1L << bitIndex); // Restores invariants
   
      checkInvariants();
      
      return this;
   }

   /**
    * Sets the bit at the specified index to the specified value.
    *
    * @param bitIndex
    *           a bit index
    * @param value
    *           a boolean value to set
    * @throws IndexOutOfBoundsException
    *            if the specified index is negative
    * @since 1.4
    */
   public MutableBitSet set(int bitIndex, boolean value)
   {
      if (value) set(bitIndex);
      else clear(bitIndex);
      
      return this;
   }

   /**
    * Sets the bits from the specified {@code fromIndex} (inclusive) to the specified {@code toIndex} (exclusive) to
    * {@code true}.
    *
    * @param fromIndex
    *           index of the first bit to be set
    * @param toIndex
    *           index after the last bit to be set
    * @throws IndexOutOfBoundsException
    *            if {@code fromIndex} is negative, or {@code toIndex} is negative, or {@code fromIndex} is larger than
    *            {@code toIndex}
    * @since 1.4
    */
   public MutableBitSet set(int fromIndex, int toIndex)
   {
      checkRange(fromIndex, toIndex);
   
      if (fromIndex == toIndex) return this;
   
      // Increase capacity if necessary
      int startWordIndex = wordIndex(fromIndex);
      int endWordIndex = wordIndex(toIndex - 1);
      expandTo(endWordIndex);
   
      long firstWordMask = WORD_MASK << fromIndex;
      long lastWordMask = WORD_MASK >>> -toIndex;
      if (startWordIndex == endWordIndex) {
         // Case 1: One word
         words[startWordIndex] |= (firstWordMask & lastWordMask);
      } else {
         // Case 2: Multiple words
         // Handle first word
         words[startWordIndex] |= firstWordMask;
   
         // Handle intermediate words, if any
         for (int i = startWordIndex + 1; i < endWordIndex; i++)
            words[i] = WORD_MASK;
   
         // Handle last word (restores invariants)
         words[endWordIndex] |= lastWordMask;
      }
   
      checkInvariants();
      
      return this;
   }

   /**
    * Sets the bits from the specified {@code fromIndex} (inclusive) to the specified {@code toIndex} (exclusive) to the
    * specified value.
    *
    * @param fromIndex
    *           index of the first bit to be set
    * @param toIndex
    *           index after the last bit to be set
    * @param value
    *           value to set the selected bits to
    * @throws IndexOutOfBoundsException
    *            if {@code fromIndex} is negative, or {@code toIndex} is negative, or {@code fromIndex} is larger than
    *            {@code toIndex}
    * @since 1.4
    */
   public MutableBitSet set(int fromIndex, int toIndex, boolean value)
   {
      if (value) set(fromIndex, toIndex);
      else clear(fromIndex, toIndex);
      
      return this;
   }

   /**
    * Sets the bit specified by the index to {@code false}.
    *
    * @param bitIndex
    *           the index of the bit to be cleared
    * @throws IndexOutOfBoundsException
    *            if the specified index is negative
    * @since JDK1.0
    */
   public MutableBitSet clear(int bitIndex)
   {
      if (bitIndex < 0) throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
   
      int wordIndex = wordIndex(bitIndex);
      if (wordIndex >= wordsInUse) return this;
   
      words[wordIndex] &= ~(1L << bitIndex);
   
      recalculateWordsInUse();
      checkInvariants();
      
      return this;
   }

   /**
    * Sets the bits from the specified {@code fromIndex} (inclusive) to the specified {@code toIndex} (exclusive) to
    * {@code false}.
    *
    * @param fromIndex
    *           index of the first bit to be cleared
    * @param toIndex
    *           index after the last bit to be cleared
    * @throws IndexOutOfBoundsException
    *            if {@code fromIndex} is negative, or {@code toIndex} is negative, or {@code fromIndex} is larger than
    *            {@code toIndex}
    * @since 1.4
    */
   public MutableBitSet clear(int fromIndex, int toIndex)
   {
      checkRange(fromIndex, toIndex);
   
      if (fromIndex == toIndex) return this;
   
      int startWordIndex = wordIndex(fromIndex);
      if (startWordIndex >= wordsInUse) return this;
   
      int endWordIndex = wordIndex(toIndex - 1);
      if (endWordIndex >= wordsInUse) {
         toIndex = length();
         endWordIndex = wordsInUse - 1;
      }
   
      long firstWordMask = WORD_MASK << fromIndex;
      long lastWordMask = WORD_MASK >>> -toIndex;
      if (startWordIndex == endWordIndex) {
         // Case 1: One word
         words[startWordIndex] &= ~(firstWordMask & lastWordMask);
      } else {
         // Case 2: Multiple words
         // Handle first word
         words[startWordIndex] &= ~firstWordMask;
   
         // Handle intermediate words, if any
         for (int i = startWordIndex + 1; i < endWordIndex; i++)
            words[i] = 0;
   
         // Handle last word
         words[endWordIndex] &= ~lastWordMask;
      }
   
      recalculateWordsInUse();
      checkInvariants();
      
      return this;
   }

   /**
    * Sets all of the bits in this BitSet to {@code false}.
    *
    * @since 1.4
    */
   public MutableBitSet clear()
   {
      while (wordsInUse > 0)
         words[--wordsInUse] = 0;
      
      return this;
   }

   /**
    * Performs a logical <b>AND</b> of this target bit set with the argument bit set. This bit set is modified so that
    * each bit in it has the value {@code true} if and only if it both initially had the value {@code true} and the
    * corresponding bit in the bit set argument also had the value {@code true}.
    *
    * @param set
    *           a bit set
    */
   public MutableBitSet and(IBitSet iset)
   {
      if (this == iset) return this;

      final AbstractBitSet set = toImpl(iset);
   
      while (wordsInUse > set.wordsInUse)
         words[--wordsInUse] = 0;
   
      // Perform logical AND on words in common
      for (int i = 0; i < wordsInUse; i++)
         words[i] &= set.words[i];
   
      recalculateWordsInUse();
      checkInvariants();
      
      return this;
   }

   /**
    * Performs a logical <b>OR</b> of this bit set with the bit set argument. This bit set is modified so that a bit in
    * it has the value {@code true} if and only if it either already had the value {@code true} or the corresponding bit
    * in the bit set argument has the value {@code true}.
    *
    * @param set
    *           a bit set
    */
   public MutableBitSet or(IBitSet iset)
   {
      if (this == iset) return this;

      final AbstractBitSet set = toImpl(iset);
   
      int wordsInCommon = Math.min(wordsInUse, set.wordsInUse);
   
      if (wordsInUse < set.wordsInUse) {
         ensureCapacity(set.wordsInUse);
         wordsInUse = set.wordsInUse;
      }
   
      // Perform logical OR on words in common
      for (int i = 0; i < wordsInCommon; i++)
         words[i] |= set.words[i];
   
      // Copy any remaining words
      if (wordsInCommon < set.wordsInUse)
         System.arraycopy(set.words, wordsInCommon, words, wordsInCommon, wordsInUse - wordsInCommon);
   
      // recalculateWordsInUse() is unnecessary
      checkInvariants();
      
      return this;
   }

   /**
    * Performs a logical <b>XOR</b> of this bit set with the bit set argument. This bit set is modified so that a bit in
    * it has the value {@code true} if and only if one of the following statements holds:
    * <ul>
    * <li>The bit initially has the value {@code true}, and the corresponding bit in the argument has the value
    * {@code false}.
    * <li>The bit initially has the value {@code false}, and the corresponding bit in the argument has the value
    * {@code true}.
    * </ul>
    *
    * @param set
    *           a bit set
    */
   public MutableBitSet xor(IBitSet iset)
   {
      if (this == iset) return this.clear();

      final AbstractBitSet set = toImpl(iset);

      int wordsInCommon = Math.min(wordsInUse, set.wordsInUse);
   
      if (wordsInUse < set.wordsInUse) {
         ensureCapacity(set.wordsInUse);
         wordsInUse = set.wordsInUse;
      }
   
      // Perform logical XOR on words in common
      for (int i = 0; i < wordsInCommon; i++)
         words[i] ^= set.words[i];
   
      // Copy any remaining words
      if (wordsInCommon < set.wordsInUse)
         System
            .arraycopy(set.words, wordsInCommon, words, wordsInCommon, set.wordsInUse - wordsInCommon);
   
      recalculateWordsInUse();
      checkInvariants();
      
      return this;
   }


   /**
    * Clears all of the bits in this {@code BitSet} whose corresponding bit is set in the specified {@code BitSet}.
    *
    * @param set
    *           the {@code BitSet} with which to mask this {@code BitSet}
    * @since 1.2
    */
   public MutableBitSet andNot(IBitSet iset)
   {
      if (this == iset) return this.clear();

      final AbstractBitSet set = toImpl(iset);

      // Perform logical (a & !b) on words in common
      for (int i = Math.min(wordsInUse, set.wordsInUse) - 1; i >= 0; i--)
         words[i] &= ~set.words[i];
   
      recalculateWordsInUse();
      checkInvariants();
      
      return this;
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
      if (!sizeIsSticky) trimToSize();
   
      try {
         AbstractBitSet result = (AbstractBitSet) super.clone();
         result.words = words.clone();
         result.checkInvariants();
         return result;
      }
      catch (CloneNotSupportedException e) {
         throw new InternalError(e);
      }
   }

   /**
    * Save the state of the {@code BitSet} instance to a stream (i.e., serialize it).
    */
   private void writeObject(ObjectOutputStream s) throws IOException
   {
      checkInvariants();
   
      if (!sizeIsSticky) trimToSize();
   
      ObjectOutputStream.PutField fields = s.putFields();
      fields.put("bits", words);
      s.writeFields();
   }

   /**
    * Reconstitute the {@code BitSet} instance from a stream (i.e., deserialize it).
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
      sizeIsSticky = (words.length > 0 && words[words.length - 1] == 0L); // heuristic
      checkInvariants();
   }

   @Override
   public boolean isMutable()
   {
      return true;
   }

   @Override
   public IMutableBitSet asMutable()
   {
      return this;
   }
}
