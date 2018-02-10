package info.jchein.mesosphere.elevator.common;

import java.io.IOException;

import org.springframework.boot.jackson.JsonComponent;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.boot.jackson.JsonObjectSerializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;

@JsonComponent
public class PassengerIdJsonComponent
{
   public static class PassengerIdJsonSerializer extends JsonObjectSerializer<PassengerId> {

      @Override
      protected void serializeObject(PassengerId value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException
      {
         // Despite the documentation warning in PassengerId's Javadoc, the class's toString() method is currently fine 
         // for memento extraction and a reversible constructor for deserialization exists as well.  The documentation is
         // merely to prevent external parties from relying on these traits.
         jgen.writeString(
            value.toString());
      }
      
   }

   public static class PassengerIdJsonDeserializer extends JsonObjectDeserializer<PassengerId> {

      @Override
      protected PassengerId deserializeObject(JsonParser jsonParser, DeserializationContext context,
         ObjectCodec codec, JsonNode tree)
      throws IOException
      {
         return new PassengerId(
            jsonParser.getText());
      }
   }
}
