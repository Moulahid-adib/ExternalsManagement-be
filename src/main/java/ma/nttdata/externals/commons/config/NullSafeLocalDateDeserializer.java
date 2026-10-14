package ma.nttdata.externals.commons.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A null-safe deserializer for {@link LocalDate} that handles:
 * <ul>
 *   <li>JSON {@code null} token (VALUE_NULL)</li>
 *   <li>The literal string {@code "null"} sent by some frontends for empty dates</li>
 *   <li>Blank strings</li>
 * </ul>
 * All of the above cases are treated as {@code null} instead of throwing a parse error.
 */
public class NullSafeLocalDateDeserializer extends LocalDateDeserializer {

    public NullSafeLocalDateDeserializer() {
        super(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        // Handle real JSON null token
        if (p.currentToken() == JsonToken.VALUE_NULL) {
            return null;
        }
        // Handle string token — guard against "null" or blank strings
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            String text = p.getText();
            if (text == null || text.isBlank() || "null".equalsIgnoreCase(text.trim())) {
                return null;
            }
        }
        return super.deserialize(p, ctx);
    }
}
