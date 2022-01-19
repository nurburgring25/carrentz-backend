package dev.burikk.carrentz.engine.api.common;

import com.fasterxml.jackson.databind.module.SimpleModule;
import dev.burikk.carrentz.engine.api.deserializer.CustomLocalDateDeserializer;
import dev.burikk.carrentz.engine.api.deserializer.CustomLocalDateTimeDeserializer;
import dev.burikk.carrentz.engine.api.deserializer.CustomLocalTimeDeserializer;
import dev.burikk.carrentz.engine.api.serializer.CustomLocalDateSerializer;
import dev.burikk.carrentz.engine.api.serializer.CustomLocalDateTimeSerializer;
import dev.burikk.carrentz.engine.api.serializer.CustomLocalTimeSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author Muhammad Irfan
 * @since 10/01/2019 17.52
 */
public class WynixModule extends SimpleModule {
    WynixModule() {
        //<editor-fold desc="Add serializer">
        this.addSerializer(LocalDate.class, new CustomLocalDateSerializer());
        this.addSerializer(LocalTime.class, new CustomLocalTimeSerializer());
        this.addSerializer(LocalDateTime.class, new CustomLocalDateTimeSerializer());
        //</editor-fold>

        //<editor-fold desc="Add deserializer">
        this.addDeserializer(LocalDate.class, new CustomLocalDateDeserializer());
        this.addDeserializer(LocalTime.class, new CustomLocalTimeDeserializer());
        this.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
        //</editor-fold>
    }
}