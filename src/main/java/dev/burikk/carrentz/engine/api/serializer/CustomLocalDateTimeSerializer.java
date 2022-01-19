package dev.burikk.carrentz.engine.api.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Muhammad Irfan
 * @since 10/08/2017 16:38
 */
public class CustomLocalDateTimeSerializer extends StdSerializer<LocalDateTime> {
    public CustomLocalDateTimeSerializer() {
        super(LocalDateTime.class);
    }

    @Override
    public void serialize(LocalDateTime mLocalDateTime, JsonGenerator mJsonGenerator, SerializerProvider mSerializerProvider) throws IOException {
        if (mLocalDateTime != null) {
            mJsonGenerator.writeString(mLocalDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
        } else {
            mJsonGenerator.writeNull();
        }
    }
}