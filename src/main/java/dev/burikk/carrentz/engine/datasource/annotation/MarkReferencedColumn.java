package dev.burikk.carrentz.engine.datasource.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.JDBCType;

/**
 * @author Muhammad Irfan
 * @since 19/06/2017 16:03
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MarkReferencedColumn {
    String value() default "";
    JDBCType jdbcType() default JDBCType.NULL;
    int referenceID() default 0;
    String alias() default "";
}