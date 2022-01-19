package dev.burikk.carrentz.engine.api.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author Muhammad Irfan
 * @since 10/08/2017 16:38
 */
public class CustomLocalDateSerializer extends StdSerializer<LocalDate> {
    public CustomLocalDateSerializer() {
        super(LocalDate.class);
    }

    @Override
    public void serialize(LocalDate mLocalDate, JsonGenerator mJsonGenerator, SerializerProvider mSerializerProvider) throws IOException {
        if (mLocalDate != null) {
            mJsonGenerator.writeString(mLocalDate.format(DateTimeFormatter.ISO_DATE));
        } else {
            mJsonGenerator.writeNull();
        }
    }
}