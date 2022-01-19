package dev.burikk.carrentz.engine.datasource.exception;

import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.util.Parameters;
import com.sun.istack.internal.NotNull;

/**
 * @author Muhammad Irfan
 * @since 20/06/2017 9:43
 */
public class NoSuchMarkTableException extends RuntimeException {
    private final Class<? extends Entity> mEntityClass;

    public NoSuchMarkTableException(@NotNull Class<? extends Entity> mEntityClass) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");

        this.mEntityClass = mEntityClass;
    }

    @Override
    public String getMessage() {
        return this.mEntityClass.getSimpleName() + " class is not annotated with @MarkTable.";
    }
}