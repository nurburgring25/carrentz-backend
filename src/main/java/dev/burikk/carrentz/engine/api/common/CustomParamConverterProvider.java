package dev.burikk.carrentz.engine.api.common;

import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.ext.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Muhammad Irfan
 * @since 01/03/2019 21.19
 */
@Provider
public class CustomParamConverterProvider implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (LocalDate.class == rawType || LocalTime.class == rawType || LocalDateTime.class == rawType) {
            return new ParamConverter<T>() {
                @SuppressWarnings("unchecked")
                @Override
                public T fromString(String value) {
                    if (LocalDate.class == rawType) {
                        if (StringUtils.isNotBlank(value)) {
                            return (T) LocalDate.parse(value, DateTimeFormatter.ISO_DATE);
                        }
                    } else if (LocalTime.class == rawType) {
                        if (StringUtils.isNotBlank(value)) {
                            return (T) LocalTime.parse(value, DateTimeFormatter.ISO_TIME);
                        }
                    } else {
                        if (StringUtils.isNotBlank(value)) {
                            return (T) LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
                        }
                    }

                    return null;
                }

                @Override
                public String toString(T value) {
                    return null;
                }
            };
        }

        return null;
    }
}