package uk.gov.justice.laa.maat.scheduled.tasks.fdc.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.io.IOException;

public class StrictBooleanDeserializer extends JsonDeserializer<Boolean> {
  @Override
  public Boolean deserialize(JsonParser p, DeserializationContext ctx)
      throws IOException {

    JsonToken token = p.currentToken();

    switch (token) {
      case VALUE_TRUE:
        return true;

      case VALUE_FALSE:
        return false;

      case VALUE_STRING:
        String value = p.getText().trim();
        if ("Y".equalsIgnoreCase(value) || "YES".equalsIgnoreCase(value)) return true;
        if ("N".equalsIgnoreCase(value) || "NO".equalsIgnoreCase(value)) return false;
        break;

      case VALUE_NULL:
        throw InvalidFormatException.from(
            p, "Boolean value cannot be null", null, Boolean.class
        );

      default:
        // Covers NUMBER, ARRAY, OBJECT, EMBEDDED_OBJECT
        break;
    }

    throw InvalidFormatException.from(
        p,
        "Expected boolean or 'Y'/'N' or 'YES'/'NO'",
        p.getText(),
        Boolean.class
    );
  }
}
