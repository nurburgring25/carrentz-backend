package dev.burikk.carrentz.engine.datasource.annotation;

import dev.burikk.carrentz.engine.datasource.enumeration.Operator;

import java.lang.annotation.*;

/**
 * @author Muhammad Irfan
 * @since 12/3/2017 10:07 AM
 */
@Repeatable(UniqueKeyMatchers.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UniqueKeyMatcher {
    int id();
    Operator operator();
    String[] value();
}