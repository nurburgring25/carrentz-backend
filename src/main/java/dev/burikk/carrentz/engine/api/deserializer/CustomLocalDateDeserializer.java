package dev.burikk.carrentz.engine.api.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author Muhammad Irfan
 * @since 10/08/2017 16:31
 */
public class CustomLocalDateDeserializer extends StdDeserializer<LocalDate> {
    public CustomLocalDateDeserializer() {
        super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser mJsonParser, DeserializationContext mDeserializationContext) throws IOException {
        if (StringUtils.isNotBlank(mJsonParser.getValueAsString())) {
            return LocalDate.parse(mJsonParser.getValueAsString(), DateTimeFormatter.ISO_DATE);
        } else {
            return null;
        }
    }
}