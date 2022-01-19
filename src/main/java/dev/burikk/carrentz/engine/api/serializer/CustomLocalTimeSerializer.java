package dev.burikk.carrentz.engine.api.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Muhammad Irfan
 * @since 10/08/2017 16:38
 */
public class CustomLocalTimeSerializer extends StdSerializer<LocalTime> {
    public CustomLocalTimeSerializer() {
        super(LocalTime.class);
    }

    @Override
    public void serialize(LocalTime mLocalTime, JsonGenerator mJsonGenerator, SerializerProvider mSerializerProvider) throws IOException {
        if (mLocalTime != null) {
            mJsonGenerator.writeString(mLocalTime.format(DateTimeFormatter.ISO_TIME));
        } else {
            mJsonGenerator.writeNull();
        }
    }
}