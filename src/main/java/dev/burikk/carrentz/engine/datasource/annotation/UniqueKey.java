package dev.burikk.carrentz.engine.datasource.annotation;

import java.lang.annotation.*;

/**
 * @author Muhammad Irfan
 * @since 19/06/2017 16:08
 */
@Repeatable(UniqueKeys.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UniqueKey {
    int value();
}