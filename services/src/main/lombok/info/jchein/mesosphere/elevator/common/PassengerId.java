package info.jchein.mesosphere.elevator.common;


import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import info.jchein.mesosphere.elevator.common.PassengerIdJsonComponent.PassengerIdJsonDeserializer;
import info.jchein.mesosphere.elevator.common.PassengerIdJsonComponent.PassengerIdJsonSerializer;
import lombok.AllArgsConstructor;


/**
 * Memento object representing a boarded passenger and used to refer to same when disembarking on arrival.
 * 
 * PassengerId's do have a meaningful toString() method to support use with audit logging, but the value returned is
 * intended to be considered opaque. Comparing two PassengerId.toString() outputs has no greater value than passing one
 * artifact to the others Object.equals() method, which will likely perform the comparison more efficiently and with
 * less possibility of false positives/negatives induced by comparing stringified PassengerId's to any other stringified
 * concept found.
 * 
 * @author jheinnic
 */
// @FieldDefaults(makeFinal=true, level=AccessLevel.PRIVATE)
@AllArgsConstructor
// @EqualsAndHashCode(doNotUseGetters = true)
@JsonSerialize(using = PassengerIdJsonSerializer.class)
@JsonDeserialize(using = PassengerIdJsonDeserializer.class)
public class PassengerId
implements Comparable<PassengerId>
{
   final long idMost;
   final long idLeast;


   PassengerId( UUID uuid )
   {
      this(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
   }


   PassengerId( String str )
   {
      final String[] components = str.split("-");
      if (components.length != 5) throw new IllegalArgumentException("Invalid UUID string: " + str);
      for (int i = 0; i < 5; i++)
         components[i] = "0x" + components[i];

      long idMost =
         Long.decode(components[0])
            .longValue();
      idMost <<= 16;
      idMost |=
         Long.decode(components[1])
            .longValue();
      idMost <<= 16;
      this.idMost =
         idMost |
            Long.decode(components[2])
               .longValue();

      long idLeast =
         Long.decode(components[3])
            .longValue();
      idLeast <<= 48;
      this.idLeast =
         idLeast |
            Long.decode(components[4])
               .longValue();
   }


   public String toString()
   {
      return (digits(this.idMost >> 32, 8) +
         "-" + digits(this.idMost >> 16, 4) + "-" + digits(this.idMost, 4) + "-" +
         digits(this.idLeast >> 48, 4) + "-" + digits(this.idLeast, 12));
   }


   /** Returns val represented by the specified number of hex digits. */
   private static String digits(long val, int digits)
   {
      long hi = 1L << (digits * 4);
      return Long.toHexString(hi | (val & (hi - 1)))
         .substring(1);
   }


   /**
    * Returns a hash code for this {@code UUID}.
    *
    * @return A hash code value for this {@code UUID}
    */
   public int hashCode()
   {
      long hilo = this.idMost ^ this.idLeast;
      return ((int) (hilo >> 32)) ^ (int) hilo;
   }


   /**
    * Compares this object to the specified object. The result is {@code
   * true} if and only if the argument is not {@code null}, is a {@code UUID} object, has the same variant, and contains
    * the same value, bit for bit, as this {@code UUID}.
    *
    * @param obj
    *           The object to be compared
    *
    * @return {@code true} if the objects are the same; {@code false} otherwise
    */
   public boolean equals(Object obj)
   {
      if ((null == obj) || (obj.getClass() != PassengerId.class)) return false;
      PassengerId id = (PassengerId) obj;
      return (this.idMost == id.idMost && this.idLeast == id.idLeast);
   }


   // Comparison Operations

   /**
    * Compares this UUID with the specified UUID.
    *
    * <p>
    * The first of two UUIDs is greater than the second if the most significant field in which the UUIDs differ is
    * greater for the first UUID.
    *
    * @param val
    *           {@code UUID} to which this {@code UUID} is to be compared
    *
    * @return -1, 0 or 1 as this {@code UUID} is less than, equal to, or greater than {@code val}
    *
    */
   @Override
   public int compareTo(PassengerId o)
   {
      // The ordering is intentionally set up so that the UUIDs can simply be numerically compared as two numbers
      // Order first by idMost, secondarily by idLeast.
      return (this.idMost < o.idMost)
         ? -1
         : (this.idMost > o.idMost)
            ? 1
            : (this.idLeast < o.idLeast)
               ? -1
               : (this.idLeast > o.idLeast)
                  ? 1 : 0;
   }
}
