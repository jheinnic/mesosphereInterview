package info.jchein.mesosphere.elevator.common.bitset;


import java.util.function.Consumer;
import java.util.stream.IntStream;

public interface IBitSet
{
   public IMutableBitSet mutableCopy(Consumer<IBitSetBuilder> director);
   
   public IMutableBitSet mutableCopy();

   public IReadOnlyBitSet readOnlyCopy(Consumer<IBitSetBuilder> director);
   
   public IReadOnlyBitSet readOnlyCopy();

   boolean isReadOnly(); 
   
   public IReadOnlyBitSet asReadOnly();
   
   boolean isMutable();

   public IMutableBitSet asMutable();

   /**
     * Returns a new byte array containing all the bits in this bit set.
     *
     * <p>More precisely, if
     * <br>{@code byte[] bytes = s.toByteArray();}
     * <br>then {@code bytes.length == (s.length()+7)/8} and
     * <br>{@code s.get(n) == ((bytes[n/8] & (1<<(n%8))) != 0)}
     * <br>for all {@code n < 8 * bytes.length}.
     *
     * @return a byte array containing a little-endian representation
     *         of all the bits in this bit set
     * @since 1.7
    */
   byte[] toByteArray();

   /**
     * Returns a new long array containing all the bits in this bit set.
     *
     * <p>More precisely, if
     * <br>{@code long[] longs = s.toLongArray();}
     * <br>then {@code longs.length == (s.length()+63)/64} and
     * <br>{@code s.get(n) == ((longs[n/64] & (1L<<(n%64))) != 0)}
     * <br>for all {@code n < 64 * longs.length}.
     *
     * @return a long array containing a little-endian representation
     *         of all the bits in this bit set
     * @since 1.7
    */
   long[] toLongArray();

   /**
     * Returns the value of the bit with the specified index. The value
     * is {@code true} if the bit with the index {@code bitIndex}
     * is currently set in this {@code IBitSet}; otherwise, the result
     * is {@code false}.
     *
     * @param  bitIndex   the bit index
     * @return the value of the bit with the specified index
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
   boolean get(int bitIndex);

   /**
     * Returns a new {@code IMutableBitSet} composed of bits from this {@code IBitSet}
     * from {@code fromIndex} (inclusive) to {@code toIndex} (exclusive).
     *
     * @param  fromIndex index of the first bit to include
     * @param  toIndex index after the last bit to include
     * @return a new {@code IBitSet} from a range of this {@code IBitSet}
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *         or {@code toIndex} is negative, or {@code fromIndex} is
     *         larger than {@code toIndex}
     * @since  1.4
     */
   IMutableBitSet get(int fromIndex, int toIndex);

   /**
     * Returns a new {@code IReadOnlyBitSet} composed of bits from this {@code IBitSet}
     * from {@code fromIndex} (inclusive) to {@code toIndex} (exclusive).
     *
     * @param  fromIndex index of the first bit to include
     * @param  toIndex index after the last bit to include
     * @return a new {@code IBitSet} from a range of this {@code IBitSet}
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
     *         or {@code toIndex} is negative, or {@code fromIndex} is
     *         larger than {@code toIndex}
     * @since  1.4
     */
   IReadOnlyBitSet getReadOnly(int fromIndex, int toIndex);

   /**
     * Returns the index of the first bit that is set to {@code true}
     * that occurs on or after the specified starting index. If no such
     * bit exists then {@code -1} is returned.
     *
     * <p>To iterate over the {@code true} bits in a {@code IBitSet},
     * use the following loop:
     *
     *  <pre> {@code
     * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
     *     // operate on index i here
     *     if (i == Integer.MAX_VALUE) {
     *         break; // or (i+1) would overflow
     *     }
     * }}</pre>
     *
     * @param  fromIndex the index to start checking from (inclusive)
     * @return the index of the next set bit, or {@code -1} if there
     *         is no such bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @since  1.4
     */
   int nextSetBit(int fromIndex);


   /**
     * Returns the index of the first bit that is set to {@code false}
     * that occurs on or after the specified starting index.
     *
     * @param  fromIndex the index to start checking from (inclusive)
     * @return the index of the next clear bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     * @since  1.4
     */
   int nextClearBit(int fromIndex);


   /**
     * Returns the index of the nearest bit that is set to {@code true}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * <p>To iterate over the {@code true} bits in a {@code IBitSet},
     * use the following loop:
     *
     *  <pre> {@code
     * for (int i = bs.length(); (i = bs.previousSetBit(i-1)) >= 0; ) {
     *     // operate on index i here
     * }}</pre>
     *
     * @param  fromIndex the index to start checking from (inclusive)
     * @return the index of the previous set bit, or {@code -1} if there
     *         is no such bit
     * @throws IndexOutOfBoundsException if the specified index is less
     *         than {@code -1}
     * @since  1.7
     */
   int previousSetBit(int fromIndex);


   /**
     * Returns the index of the nearest bit that is set to {@code false}
     * that occurs on or before the specified starting index.
     * If no such bit exists, or if {@code -1} is given as the
     * starting index, then {@code -1} is returned.
     *
     * @param  fromIndex the index to start checking from (inclusive)
     * @return the index of the previous clear bit, or {@code -1} if there
     *         is no such bit
     * @throws IndexOutOfBoundsException if the specified index is less
     *         than {@code -1}
     * @since  1.7
     */
   int previousClearBit(int fromIndex);


   /**
     * Returns the "logical size" of this {@code IBitSet}: the index of
     * the highest set bit in the {@code IBitSet} plus one. Returns zero
     * if the {@code IBitSet} contains no set bits.
     *
     * @return the logical size of this {@code IBitSet}
     * @since  1.2
     */
   int length();


   /**
     * Returns true if this {@code IBitSet} contains no bits that are set
     * to {@code true}.
     *
     * @return boolean indicating whether this {@code IBitSet} is empty
     * @since  1.4
     */
   boolean isEmpty();


   /**
     * Returns true if the specified {@code IBitSet} has any bits set to
     * {@code true} that are also set to {@code true} in this {@code IBitSet}.
     *
     * @param  set {@code BitSet} to intersect with
     * @return boolean indicating whether this {@code IBitSet} intersects
     *         the specified {@code BitSet}
     * @since  1.4
     */
   boolean intersects(IBitSet set);


   /**
     * Returns the number of bits set to {@code true} in this {@code IBitSet}.
     *
     * @return the number of bits set to {@code true} in this {@code IBitSet}
     * @since  1.4
     */
   int cardinality();


   /**
     * Returns the hash code value for this bit set. The hash code depends
     * only on which bits are set within this {@code IBitSet}.
     *
     * <p>The hash code is defined to be the result of the following
     * calculation:
     *  <pre> {@code
     * public int hashCode() {
     *     long h = 1234;
     *     long[] words = toLongArray();
     *     for (int i = words.length; --i >= 0; )
     *         h ^= words[i] * (i + 1);
     *     return (int)((h >> 32) ^ h);
     * }}</pre>
     * Note that the hash code changes if the set of bits is altered.
     *
     * @return the hash code value for this bit set
     */
   int hashCode();


   /**
     * Returns the number of bits of space actually in use by this
     * {@code IBitSet} to represent bit values.
     * The maximum element in the set is the size - 1st element.
     *
     * @return the number of bits currently in this bit set
     */
   int size();

   /**
     * Returns a stream of indices for which this {@code IBitSet}
     * contains a bit in the set state. The indices are returned
     * in order, from lowest to highest. The size of the stream
     * is the number of bits in the set state, equal to the value
     * returned by the {@link #cardinality()} method.
     *
     * @return a stream of integers representing set indices
     * @since 1.8
     */
   IntStream stream();
}
