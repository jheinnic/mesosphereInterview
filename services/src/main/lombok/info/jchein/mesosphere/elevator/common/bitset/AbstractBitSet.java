package info.jchein.mesosphere.elevator.common.bitset;


import java.io.ObjectStreamField;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;


/**
 * This class implements a vector of bits that grows as needed. Each component of the bit set has a {@code boolean}
 * value. The bits of a {@code BitSet} are indexed by nonnegative integers. Individual indexed bits can be examined,
 * set, or cleared. One {@code BitSet} may be used to modify the contents of another {@code BitSet} through logical AND,
 * logical inclusive OR, and logical exclusive OR operations.
 *
 * <p>
 * By default, all bits in the set initially have the value {@code false}.
 *
 * <p>
 * Every bit set has a current size, which is the number of bits of space currently in use by the bit set. Note that the
 * size is related to the implementation of a bit set, so it may change with implementation. The length of a bit set
 * relates to logical length of a bit set and is defined independently of implementation.
 *
 * <p>
 * Unless otherwise noted, passing a null parameter to any of the methods in a {@code BitSet} will result in a
 * {@code NullPointerException}.
 *
 * <p>
 * A {@code BitSet} is not safe for multithreaded use without external synchronization.
 *
 * @author Arthur van Hoff
 * @author Michael McCloskey
 * @author Martin Buchholz
 * @since JDK1.0
 */
abstract class AbstractBitSet
implements Cloneable, java.io.Serializable, IBitSet
{
   /*
    * BitSets are packed into arrays of "words." Currently a word is a long, which consists of 64 bits, requiring 6
    * address bits. The choice of word size is determined purely by performance concerns.
    */
   private final static int ADDRESS_BITS_PER_WORD = 6;
   private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
   private final static int BIT_INDEX_MASK = BITS_PER_WORD - 1;

   /* Used to shift left or right for a partial word mask */
   protected static final long WORD_MASK = 0xffffffffffffffffL;

   /**
    * @serialField bits
    *                 long[]
    *
    *                 The bits in this BitSet. The ith bit is stored in bits[i/64] at bit position i % 64 (where bit
    *                 position 0 refers to the least significant bit and 63 refers to the most significant bit).
    */
   private static final ObjectStreamField[] serialPersistentFields = {};

   /**
    * The internal field corresponding to the serialField "bits".
    */
   protected long[] words;

   /**
    * The number of words in the logical size of this BitSet.
    */
   protected transient int wordsInUse = 0;

   

   /* use serialVersionUID from JDK 1.0.2 for interoperability */
//   private static final long serialVersionUID = 7997698588986878753L;


   /**
    * Given a bit index, return word index containing it.
    */
   protected static int wordIndex(int bitIndex)
   {
      return bitIndex >> ADDRESS_BITS_PER_WORD;
   }


   /**
    * Every public method must preserve these invariants.
    */
   protected void checkInvariants()
   {
      assert (wordsInUse == 0 || words[wordsInUse - 1] != 0);
      assert (wordsInUse >= 0 && wordsInUse <= words.length);
      assert (wordsInUse == words.length || words[wordsInUse] == 0);
   }


   /**
    * Sets the field wordsInUse to the logical size in words of the bit set. WARNING:This method assumes that the number
    * of words actually in use is less than or equal to the current value of wordsInUse!
    */
   protected void recalculateWordsInUse()
   {
      // Traverse the bitset until a used word is found
      int i;
      for (i = wordsInUse - 1; i >= 0; i--)
         if (words[i] != 0) break;

      wordsInUse = i + 1; // The new logical size
   }


   /**
    * Creates a new bit set. All bits are initially {@code false}.
    */
   protected AbstractBitSet()
   {
      initWords(BITS_PER_WORD);
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
   protected AbstractBitSet( int nbits )
   {
      // nbits can't be negative; size 0 is OK
      if (nbits < 0) throw new NegativeArraySizeException("nbits < 0: " + nbits);

      initWords(nbits);
   }


   private void initWords(int nbits)
   {
      words = new long[wordIndex(nbits - 1) + 1];
   }


   /**
    * Creates a bit set using words as the internal representation. The last word (if there is one) must be non-zero.
    */
   protected AbstractBitSet( long[] words )
   {
      this.words = words;
      this.wordsInUse = words.length;
      checkInvariants();
   }


   /**
    * Returns a new byte array containing all the bits in this bit set.
    *
    * <p>
    * More precisely, if <br>
    * {@code byte[] bytes = s.toByteArray();} <br>
    * then {@code bytes.length == (s.length()+7)/8} and <br>
    * {@code s.get(n) == ((bytes[n/8] & (1<<(n%8))) != 0)} <br>
    * for all {@code n < 8 * bytes.length}.
    *
    * @return a byte array containing a little-endian representation of all the bits in this bit set
    * @since 1.7
    */
   public byte[] toByteArray()
   {
      int n = wordsInUse;
      if (n == 0) return new byte[0];
      int len = 8 * (n - 1);
      for (long x = words[n - 1]; x != 0; x >>>= 8)
         len++;
      byte[] bytes = new byte[len];
      ByteBuffer bb =
         ByteBuffer.wrap(bytes)
            .order(ByteOrder.LITTLE_ENDIAN);
      for (int i = 0; i < n - 1; i++)
         bb.putLong(words[i]);
      for (long x = words[n - 1]; x != 0; x >>>= 8)
         bb.put((byte) (x & 0xff));
      return bytes;
   }


   /**
    * Returns a new long array containing all the bits in this bit set.
    *
    * <p>
    * More precisely, if <br>
    * {@code long[] longs = s.toLongArray();} <br>
    * then {@code longs.length == (s.length()+63)/64} and <br>
    * {@code s.get(n) == ((longs[n/64] & (1L<<(n%64))) != 0)} <br>
    * for all {@code n < 64 * longs.length}.
    *
    * @return a long array containing a little-endian representation of all the bits in this bit set
    * @since 1.7
    */
   public long[] toLongArray()
   {
      return Arrays.copyOf(words, wordsInUse);
   }


   /**
    * Checks that fromIndex ... toIndex is a valid range of bit indices.
    */
   protected static void checkRange(int fromIndex, int toIndex)
   {
      if (fromIndex < 0) throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
      if (toIndex < 0) throw new IndexOutOfBoundsException("toIndex < 0: " + toIndex);
      if (fromIndex > toIndex)
         throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " > toIndex: " + toIndex);
   }


   /**
    * Returns the value of the bit with the specified index. The value is {@code true} if the bit with the index
    * {@code bitIndex} is currently set in this {@code BitSet}; otherwise, the result is {@code false}.
    *
    * @param bitIndex
    *           the bit index
    * @return the value of the bit with the specified index
    * @throws IndexOutOfBoundsException
    *            if the specified index is negative
    */
   public boolean get(int bitIndex)
   {
      if (bitIndex < 0) throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

      checkInvariants();

      int wordIndex = wordIndex(bitIndex);
      return (wordIndex < wordsInUse) && ((words[wordIndex] & (1L << bitIndex)) != 0);
   }


   /**
    * Returns a new {@code BitSet} composed of bits from this {@code BitSet} from {@code fromIndex} (inclusive) to
    * {@code toIndex} (exclusive).
    *
    * @param fromIndex
    *           index of the first bit to include
    * @param toIndex
    *           index after the last bit to include
    * @return a new {@code BitSet} from a range of this {@code BitSet}
    * @throws IndexOutOfBoundsException
    *            if {@code fromIndex} is negative, or {@code toIndex} is negative, or {@code fromIndex} is larger than
    *            {@code toIndex}
    * @since 1.4
    */
   public MutableBitSet get(int fromIndex, int toIndex)
   {
      checkRange(fromIndex, toIndex);

      checkInvariants();

      int len = length();

      // If no set bits in range return empty bitset
      if (len <= fromIndex || fromIndex == toIndex) return new MutableBitSet(0);

      // An optimization
      if (toIndex > len) toIndex = len;

      MutableBitSet result = new MutableBitSet(toIndex - fromIndex);
      this.doGetTo(result, fromIndex, toIndex);
      return result;
   }


   public ReadOnlyBitSet getReadOnly(int fromIndex, int toIndex)
   {
      checkRange(fromIndex, toIndex);

      checkInvariants();

      int len = length();

      // If no set bits in range return empty bitset
      if (len <= fromIndex || fromIndex == toIndex) return new ReadOnlyBitSet(0);

      // An optimization
      if (toIndex > len) toIndex = len;

      ReadOnlyBitSet result = new ReadOnlyBitSet(toIndex - fromIndex);
      this.doGetTo(result, fromIndex, toIndex);
      return result;
   }


   private void doGetTo(AbstractBitSet result, int fromIndex, int toIndex)
   {
      int targetWords = wordIndex(toIndex - fromIndex - 1) + 1;
      int sourceIndex = wordIndex(fromIndex);
      boolean wordAligned = ((fromIndex & BIT_INDEX_MASK) == 0);

      // Process all words but the last word
      for (int i = 0; i < targetWords - 1; i++, sourceIndex++)
         result.words[i] =
            wordAligned
               ? words[sourceIndex]
               : (words[sourceIndex] >>> fromIndex) | (words[sourceIndex + 1] << -fromIndex);

      // Process the last word
      long lastWordMask = WORD_MASK >>> -toIndex;
      result.words[targetWords - 1] =
         ((toIndex - 1) & BIT_INDEX_MASK) < (fromIndex & BIT_INDEX_MASK) ? /* straddles source words */
            ((words[sourceIndex] >>> fromIndex) | (words[sourceIndex + 1] & lastWordMask) << -fromIndex)
            : ((words[sourceIndex] & lastWordMask) >>> fromIndex);

      // Set wordsInUse correctly
      result.wordsInUse = targetWords;
      result.recalculateWordsInUse();
      result.checkInvariants();
   }
   
   @Override
   public MutableBitSet mutableCopy(Consumer<IBitSetBuilder> director) {
      BitSetFactoryBuilder factoryBuilder = new BitSetFactoryBuilder();
      director.accept(factoryBuilder);
      return factoryBuilder.buildMutable();
   }

   @Override
   public ReadOnlyBitSet readOnlyCopy(Consumer<IBitSetBuilder> director) {
      BitSetFactoryBuilder factoryBuilder = new BitSetFactoryBuilder();
      director.accept(factoryBuilder);
      return factoryBuilder.buildReadOnly();
   }

   @Override
   public IMutableBitSet mutableCopy()
   {
      return new MutableBitSet(
         Arrays.copyOf(words, wordsInUse));
   }


   @Override
   public IReadOnlyBitSet readOnlyCopy()
   {
      return new ReadOnlyBitSet(
         Arrays.copyOf(words, wordsInUse));
   }

   @Override
   public boolean isReadOnly() {
      return false;
   }

   @Override
   public boolean isMutable()
   {
      return false;
   }

   @Override
   public IReadOnlyBitSet asReadOnly()
   {
      throw new UnsupportedOperationException();
   }


   @Override
   public IMutableBitSet asMutable()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Returns the index of the first bit that is set to {@code true} that occurs on or after the specified starting
    * index. If no such bit exists then {@code -1} is returned.
    *
    * <p>
    * To iterate over the {@code true} bits in a {@code BitSet}, use the following loop:
    *
    * <pre>
    *  {@code
    * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
    *     // operate on index i here
    *     if (i == Integer.MAX_VALUE) {
    *         break; // or (i+1) would overflow
    *     }
    * }}
    * </pre>
    *
    * @param fromIndex
    *           the index to start checking from (inclusive)
    * @return the index of the next set bit, or {@code -1} if there is no such bit
    * @throws IndexOutOfBoundsException
    *            if the specified index is negative
    * @since 1.4
    */
   public int nextSetBit(int fromIndex)
   {
      if (fromIndex < 0) throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

      checkInvariants();

      int u = wordIndex(fromIndex);
      if (u >= wordsInUse) return -1;

      long word = words[u] & (WORD_MASK << fromIndex);

      while (true) {
         if (word != 0) return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
         if (++u == wordsInUse) return -1;
         word = words[u];
      }
   }


   /**
    * Returns the index of the first bit that is set to {@code false} that occurs on or after the specified starting
    * index.
    *
    * @param fromIndex
    *           the index to start checking from (inclusive)
    * @return the index of the next clear bit
    * @throws IndexOutOfBoundsException
    *            if the specified index is negative
    * @since 1.4
    */
   public int nextClearBit(int fromIndex)
   {
      // Neither spec nor implementation handle bitsets of maximal length.
      // See 4816253.
      if (fromIndex < 0) throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

      checkInvariants();

      int u = wordIndex(fromIndex);
      if (u >= wordsInUse) return fromIndex;

      long word = ~words[u] & (WORD_MASK << fromIndex);

      while (true) {
         if (word != 0) return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
         if (++u == wordsInUse) return wordsInUse * BITS_PER_WORD;
         word = ~words[u];
      }
   }


   /**
    * Returns the index of the nearest bit that is set to {@code true} that occurs on or before the specified starting
    * index. If no such bit exists, or if {@code -1} is given as the starting index, then {@code -1} is returned.
    *
    * <p>
    * To iterate over the {@code true} bits in a {@code BitSet}, use the following loop:
    *
    * <pre>
    *  {@code
    * for (int i = bs.length(); (i = bs.previousSetBit(i-1)) >= 0; ) {
    *     // operate on index i here
    * }}
    * </pre>
    *
    * @param fromIndex
    *           the index to start checking from (inclusive)
    * @return the index of the previous set bit, or {@code -1} if there is no such bit
    * @throws IndexOutOfBoundsException
    *            if the specified index is less than {@code -1}
    * @since 1.7
    */
   public int previousSetBit(int fromIndex)
   {
      if (fromIndex < 0) {
         if (fromIndex == -1) return -1;
         throw new IndexOutOfBoundsException("fromIndex < -1: " + fromIndex);
      }

      checkInvariants();

      int u = wordIndex(fromIndex);
      if (u >= wordsInUse) return length() - 1;

      long word = words[u] & (WORD_MASK >>> -(fromIndex + 1));

      while (true) {
         if (word != 0) return (u + 1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word);
         if (u-- == 0) return -1;
         word = words[u];
      }
   }


   /**
    * Returns the index of the nearest bit that is set to {@code false} that occurs on or before the specified starting
    * index. If no such bit exists, or if {@code -1} is given as the starting index, then {@code -1} is returned.
    *
    * @param fromIndex
    *           the index to start checking from (inclusive)
    * @return the index of the previous clear bit, or {@code -1} if there is no such bit
    * @throws IndexOutOfBoundsException
    *            if the specified index is less than {@code -1}
    * @since 1.7
    */
   public int previousClearBit(int fromIndex)
   {
      if (fromIndex < 0) {
         if (fromIndex == -1) return -1;
         throw new IndexOutOfBoundsException("fromIndex < -1: " + fromIndex);
      }

      checkInvariants();

      int u = wordIndex(fromIndex);
      if (u >= wordsInUse) return fromIndex;

      long word = ~words[u] & (WORD_MASK >>> -(fromIndex + 1));

      while (true) {
         if (word != 0) return (u + 1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word);
         if (u-- == 0) return -1;
         word = ~words[u];
      }
   }


   /**
    * Returns the "logical size" of this {@code BitSet}: the index of the highest set bit in the {@code BitSet} plus
    * one. Returns zero if the {@code BitSet} contains no set bits.
    *
    * @return the logical size of this {@code BitSet}
    * @since 1.2
    */
   public int length()
   {
      if (wordsInUse == 0) return 0;

      return BITS_PER_WORD * (wordsInUse - 1) +
         (BITS_PER_WORD - Long.numberOfLeadingZeros(words[wordsInUse - 1]));
   }


   /**
    * Returns true if this {@code BitSet} contains no bits that are set to {@code true}.
    *
    * @return boolean indicating whether this {@code BitSet} is empty
    * @since 1.4
    */
   public boolean isEmpty()
   {
      return wordsInUse == 0;
   }


   /**
    * Returns true if the specified {@code BitSet} has any bits set to {@code true} that are also set to {@code true} in
    * this {@code BitSet}.
    *
    * @param set
    *           {@code BitSet} to intersect with
    * @return boolean indicating whether this {@code BitSet} intersects the specified {@code BitSet}
    * @since 1.4
    */
   public boolean intersects(IBitSet set)
   {
      AbstractBitSet setImpl;
      if (set instanceof AbstractBitSet) {
         setImpl = (AbstractBitSet) set;
      } else {
         // In practice, this extra copy will rarely ever happen--most consumers will have no reason to re-implement IBitSet directly, but this allows
         // us to define intersect in terms of IBitSet instead of AbstractBitSet without having either loss of generality or falling back to either 
         // conditionally throwing UnsupportedOperationException or ClassCastException.
         setImpl = new ReadOnlyBitSet(set.toLongArray());
      }
      for (int i = Math.min(wordsInUse, setImpl.wordsInUse) - 1; i >= 0; i--)
         if ((words[i] & setImpl.words[i]) != 0) return true;
      return false;
   }


   /**
    * Returns the number of bits set to {@code true} in this {@code BitSet}.
    *
    * @return the number of bits set to {@code true} in this {@code BitSet}
    * @since 1.4
    */
   public int cardinality()
   {
      int sum = 0;
      for (int i = 0; i < wordsInUse; i++)
         sum += Long.bitCount(words[i]);
      return sum;
   }


   /**
    * Returns the hash code value for this bit set. The hash code depends only on which bits are set within this
    * {@code BitSet}.
    *
    * <p>
    * The hash code is defined to be the result of the following calculation:
    * 
    * <pre>
    *  {@code
    * public int hashCode() {
    *     long h = 1234;
    *     long[] words = toLongArray();
    *     for (int i = words.length; --i >= 0; )
    *         h ^= words[i] * (i + 1);
    *     return (int)((h >> 32) ^ h);
    * }}
    * </pre>
    * 
    * Note that the hash code changes if the set of bits is altered.
    *
    * @return the hash code value for this bit set
    */
   public int hashCode()
   {
      long h = 1234;
      for (int i = wordsInUse; --i >= 0;)
         h ^= words[i] * (i + 1);

      return (int) ((h >> 32) ^ h);
   }


   /**
    * Returns the number of bits of space actually in use by this {@code BitSet} to represent bit values. The maximum
    * element in the set is the size - 1st element.
    *
    * @return the number of bits currently in this bit set
    */
   public int size()
   {
      return words.length * BITS_PER_WORD;
   }


   /**
    * Compares this object against the specified object. The result is {@code true} if and only if the argument is not
    * {@code null} and is a {@code Bitset} object that has exactly the same set of bits set to {@code true} as this bit
    * set. That is, for every nonnegative {@code int} index {@code k},
    * 
    * <pre>
    * ((BitSet) obj).get(k) == this.get(k)
    * </pre>
    * 
    * must be true. The current sizes of the two bit sets are not compared.
    *
    * @param obj
    *           the object to compare with
    * @return {@code true} if the objects are the same; {@code false} otherwise
    * @see #size()
    */
   public boolean equals(Object obj)
   {
      if (!(obj instanceof AbstractBitSet)) return false;
      if (this == obj) return true;

      AbstractBitSet set = (AbstractBitSet) obj;

      checkInvariants();
      set.checkInvariants();

      if (wordsInUse != set.wordsInUse) return false;

      // Check words in use by both BitSets
      for (int i = 0; i < wordsInUse; i++)
         if (words[i] != set.words[i]) return false;

      return true;
   }


   /**
    * Attempts to reduce internal storage used for the bits in this bit set. Calling this method may, but is not
    * required to, affect the value returned by a subsequent call to the {@link #size()} method.
    */
   protected void trimToSize()
   {
      if (wordsInUse != words.length) {
         words = Arrays.copyOf(words, wordsInUse);
         checkInvariants();
      }
   }


   /**
    * Returns a string representation of this bit set. For every index for which this {@code BitSet} contains a bit in
    * the set state, the decimal representation of that index is included in the result. Such indices are listed in
    * order from lowest to highest, separated by ",&nbsp;" (a comma and a space) and surrounded by braces, resulting in
    * the usual mathematical notation for a set of integers.
    *
    * <p>
    * Example:
    * 
    * <pre>
    * BitSet drPepper = new BitSet();
    * </pre>
    * 
    * Now {@code drPepper.toString()} returns "{@code {}}".
    * 
    * <pre>
    * drPepper.set(2);
    * </pre>
    * 
    * Now {@code drPepper.toString()} returns "{@code {2}}".
    * 
    * <pre>
    * drPepper.set(4);
    * drPepper.set(10);
    * </pre>
    * 
    * Now {@code drPepper.toString()} returns "{@code {2, 4, 10}}".
    *
    * @return a string representation of this bit set
    */
   public String toString()
   {
      checkInvariants();

      int numBits = (wordsInUse > 128) ? cardinality() : wordsInUse * BITS_PER_WORD;
      StringBuilder b = new StringBuilder(6 * numBits + 2);
      b.append('{');

      int i = nextSetBit(0);
      if (i != -1) {
         b.append(i);
         while (true) {
            if (++i < 0) break;
            if ((i = nextSetBit(i)) < 0) break;
            int endOfRun = nextClearBit(i);
            do {
               b.append(", ")
                  .append(i);
            }
            while (++i != endOfRun);
         }
      }

      b.append('}');
      return b.toString();
   }


   /**
    * Returns a stream of indices for which this {@code BitSet} contains a bit in the set state. The indices are
    * returned in order, from lowest to highest. The size of the stream is the number of bits in the set state, equal to
    * the value returned by the {@link #cardinality()} method.
    *
    * <p>
    * The bit set must remain constant during the execution of the terminal stream operation. Otherwise, the result of
    * the terminal stream operation is undefined.
    *
    * @return a stream of integers representing set indices
    * @since 1.8
    */
   public IntStream stream()
   {
      class BitSetIterator
      implements PrimitiveIterator.OfInt
      {
         int next = nextSetBit(0);


         public boolean hasNext()
         {
            return next != -1;
         }


         public int nextInt()
         {
            if (next != -1) {
               int ret = next;
               next = nextSetBit(next + 1);
               return ret;
            } else {
               throw new NoSuchElementException();
            }
         }
      }

      return StreamSupport.intStream(
         () -> Spliterators.spliterator(
            new BitSetIterator(),
            cardinality(),
            Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SORTED),
         Spliterator.SIZED |
            Spliterator.SUBSIZED | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SORTED,
         false);
   }
   

   protected AbstractBitSet toImpl(IBitSet iset)
   {
      final AbstractBitSet set;
      if (! (iset instanceof AbstractBitSet)) {
         set = new ReadOnlyBitSet(iset.toLongArray());
      } else {
         set = (AbstractBitSet) iset;
      }
      return set;
   }


   private class BitSetFactoryBuilder
   implements IBitSetBuilder
   {
      private MutableBitSet prototype;

      private BitSetFactoryBuilder( )
      {
         this.prototype = new MutableBitSet(AbstractBitSet.this.words);
      }


      @Override
      public IBitSetBuilder flip(int bitIndex)
      {
         this.prototype.flip(bitIndex);
         return this;
      }


      @Override
      public IBitSetBuilder flip(int fromIndex, int toIndex)
      {
         this.prototype.flip(fromIndex, toIndex);
         return this;
      }


      @Override
      public IBitSetBuilder set(int bitIndex)
      {
         this.prototype.set(bitIndex);
         return this;
      }


      @Override
      public IBitSetBuilder set(int bitIndex, boolean value)
      {
         this.prototype.set(bitIndex, value);
         return this;
      }


      @Override
      public IBitSetBuilder set(int fromIndex, int toIndex)
      {
         this.prototype.set(fromIndex, toIndex);
         return this;
      }


      @Override
      public IBitSetBuilder set(int fromIndex, int toIndex, boolean value)
      {
         this.prototype.set(fromIndex, toIndex, value);
         return this;
      }


      @Override
      public IBitSetBuilder clear(int bitIndex)
      {
         this.prototype.clear(bitIndex);
         return this;
      }


      @Override
      public IBitSetBuilder clear(int fromIndex, int toIndex)
      {
         this.prototype.clear(fromIndex, toIndex);
         return this;
      }


      @Override
      public IBitSetBuilder clear()
      {
         this.prototype.clear();
         return this;
      }


      @Override
      public IBitSetBuilder and(IBitSet set)
      {
         this.prototype.and(set);
         return this;
      }


      @Override
      public IBitSetBuilder or(IBitSet set)
      {
         this.prototype.or(set);
         return this;
      }


      @Override
      public IBitSetBuilder xor(IBitSet set)
      {
         this.prototype.xor(set);
         return this;
      }


      @Override
      public IBitSetBuilder andNot(IBitSet set)
      {
         this.prototype.andNot(set);
         return this;
      }


      MutableBitSet buildMutable()
      {
         return (MutableBitSet) this.prototype.clone();
      }


      ReadOnlyBitSet buildReadOnly()
      {
         return new ReadOnlyBitSet(this.prototype.words);
      }
   }
}
