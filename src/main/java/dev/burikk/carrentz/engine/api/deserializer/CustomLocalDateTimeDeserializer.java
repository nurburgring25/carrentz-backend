package dev.burikk.carrentz.engine.api.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Muhammad Irfan
 * @since 10/08/2017 16:31
 */
public class CustomLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {
    public CustomLocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser mJsonParser, DeserializationContext mDeserializationContext) throws IOException {
        if (StringUtils.isNotBlank(mJsonParser.getValueAsString())) {
            return LocalDateTime.parse(mJsonParser.getValueAsString(), DateTimeFormatter.ISO_DATE_TIME);
        } else {
            return null;
        }
    }
}