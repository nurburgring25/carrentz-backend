package dev.burikk.carrentz.engine.datasource.annotation;

import dev.burikk.carrentz.engine.datasource.enumeration.JoinType;

import java.lang.annotation.*;

/**
 * @author Muhammad Irfan
 * @since 19/06/2017 16:05
 */
@Repeatable(References.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Reference {
    int id();
    String sourceColumn();
    String sourceTable() default "";
    String targetColumn() default "id";
    String targetTable();
    String targetAliasTable() default "";
    boolean checkOnDelete() default true;
    JoinType joinType() default JoinType.INNER_JOIN;
}