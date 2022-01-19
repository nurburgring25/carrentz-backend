package dev.burikk.carrentz.engine.datasource.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Muhammad Irfan
 * @since 12/3/2017 10:08 AM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UniqueKeyMatchers {
    UniqueKeyMatcher[] value();
}