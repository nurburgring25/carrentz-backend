package dev.burikk.carrentz.engine.api.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * @author Muhammad Irfan
 * @since 31/10/2017 11:43
 */
@Provider
public class JacksonConfigurator implements ContextResolver<ObjectMapper> {
    private ObjectMapper objectMapper;

    public JacksonConfigurator() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.registerModule(new Jdk8Module());
        this.objectMapper.registerModule(new WynixModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public ObjectMapper getContext(Class<?> mClass) {
        return this.objectMapper;
    }
}