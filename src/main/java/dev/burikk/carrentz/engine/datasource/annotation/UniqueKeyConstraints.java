package dev.burikk.carrentz.engine.datasource.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Muhammad Irfan
 * @since 19/06/2017 16:09
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UniqueKeyConstraints {
    UniqueKeyConstraint[] value();
}