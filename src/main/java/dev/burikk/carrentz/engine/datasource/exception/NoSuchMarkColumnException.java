package dev.burikk.carrentz.engine.datasource.exception;

import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.util.Parameters;
import com.sun.istack.internal.NotNull;

import java.lang.reflect.Field;

/**
 * @author Muhammad Irfan
 * @since 20/06/2017 9:23
 */
public class NoSuchMarkColumnException extends RuntimeException {
    private final Class<? extends Entity> mEntityClass;
    private final Field mField;

    public NoSuchMarkColumnException(
            @NotNull Class<? extends Entity> mEntityClass,
            @NotNull Field mField
    ) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");
        Parameters.requireNotNull(mField, "mField");

        this.mEntityClass = mEntityClass;
        this.mField = mField;
    }

    @Override
    public String getMessage() {
        return this.mField.getName() + " field in " + this.mEntityClass.getSimpleName() + " class, is not annotated with @MarkColumn.";
    }
}