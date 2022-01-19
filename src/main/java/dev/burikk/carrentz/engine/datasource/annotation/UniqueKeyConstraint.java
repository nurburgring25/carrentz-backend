package dev.burikk.carrentz.engine.datasource.annotation;

import java.lang.annotation.*;

/**
 * @author Muhammad Irfan
 * @since 19/06/2017 16:09
 */
@Repeatable(UniqueKeyConstraints.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UniqueKeyConstraint {
    int id();
    String value();
    boolean includeDeleted() default true;
    boolean deferrable() default false;
    boolean autoValidate() default true;
    String violationMessage() default "";
}