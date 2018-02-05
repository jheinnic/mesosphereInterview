package info.jchein.mesosphere.elevator.common;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal=true, level=AccessLevel.PRIVATE)
@AllArgsConstructor 
@EqualsAndHashCode(doNotUseGetters=true)
public class PassengerId implements IPassengerId
{
   private final long idLeast;
   private final long idMost;

   PassengerId(UUID uuid) {
      this.idLeast = uuid.getLeastSignificantBits();
      this.idMost = uuid.getMostSignificantBits();
   }

   public String toString() {
      return new UUID(this.idMost, this.idLeast).toString();
   }
}
