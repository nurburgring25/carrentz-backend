package dev.burikk.carrentz.engine.api.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Muhammad Irfan
 * @since 10/08/2017 16:31
 */
public class CustomLocalTimeDeserializer extends StdDeserializer<LocalTime> {
    public CustomLocalTimeDeserializer() {
        super(LocalTime.class);
    }

    @Override
    public LocalTime deserialize(JsonParser mJsonParser, DeserializationContext mDeserializationContext) throws IOException {
        if (StringUtils.isNotBlank(mJsonParser.getValueAsString())) {
            return LocalTime.parse(mJsonParser.getValueAsString(), DateTimeFormatter.ISO_TIME);
        } else {
            return null;
        }
    }
}