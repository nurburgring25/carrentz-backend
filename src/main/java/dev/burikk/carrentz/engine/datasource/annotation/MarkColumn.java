package dev.burikk.carrentz.engine.datasource.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.JDBCType;

/**
 * @author Muhammad Irfan
 * @since 19/06/2017 16:01
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MarkColumn {
    String value() default "";
    JDBCType jdbcType() default JDBCType.NULL;
    int maxLength() default 0;
    boolean isNotNull() default false;
    String defaultValue() default "";
    String columnReference() default "";
    String tableReference() default "";
}