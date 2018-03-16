package info.jchein.mesosphere.elevator.common.graph;

import java.util.BitSet;

import lombok.experimental.Delegate;


public class ProtectedBitSet implements ReadOnlyBitSet {
   @Delegate(types={ReadOnlyBitSet.class})
   private BitSet bitSet;
   
   ProtectedBitSet(BitSet src) {
      this.bitSet = (BitSet) src.clone();
   }

   ProtectedBitSet(BitSet src, boolean alreadyCloned) {
      if (alreadyCloned) {
         this.bitSet = src;
      } else {
         this.bitSet = (BitSet) src.clone();
      }
   }

   /**
     * Copying a {@code ProtectedBitSet} produces a new {@code BitSet}
     * that is a mutable equivalent of it.
     * The clone of the bit set is another bit set that has exactly 
     * same bits set to {@code true} as this protected bit set, but it is 
     * not read only.
     *
     * @return a clone of this bit set
     * @see    #size()
     */
   public BitSet copy() {
      return (BitSet) this.bitSet.clone();
   }

   public boolean intersects(ProtectedBitSet other) {
      return this.bitSet.intersects(other.bitSet);
   }
   
//   BitSet exposeInternally() {
//      return this.bitSet;
//   }
}