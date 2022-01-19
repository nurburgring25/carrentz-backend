package dev.burikk.carrentz.app;

import dev.burikk.carrentz.engine.api.common.CustomParamConverterProvider;
import dev.burikk.carrentz.engine.api.common.JacksonConfigurator;
import dev.burikk.carrentz.engine.api.exception.mapper.GeneralExceptionMapper;
import dev.burikk.carrentz.engine.common.Constant;
import dev.burikk.carrentz.engine.common.LanguageManager;
import dev.burikk.carrentz.engine.datasource.ConnectionManager;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Muhammad Irfan
 * @since 03/04/2019 14.40
 */
public class Igniter extends Application {
    public Igniter(@Context ServletContext servletContext) {
        if (Constant.Application.EMBED) {
            LanguageManager.init();
        } else {
            LanguageManager.init(servletContext);
        }

        ConnectionManager.init();
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> objects = new HashSet<>();

        objects.add(new JacksonConfigurator());

        return objects;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> mMap = new HashMap<>();

        mMap.put("jersey.config.disableMoxyJson.server", true);

        return mMap;
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        classes.add(CustomParamConverterProvider.class);
        classes.add(GeneralExceptionMapper.class);

        return classes;
    }
}