package dev.burikk.carrentz.engine.datasource.exception;

import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.util.Parameters;
import com.sun.istack.internal.NotNull;

/**
 * @author Muhammad Irfan
 * @since 20/06/2017 9:12
 */
public class NoSuchPrimaryKeyException extends RuntimeException {
    private final Class<? extends Entity> mEntityClass;

    public NoSuchPrimaryKeyException(@NotNull Class<? extends Entity> mEntityClass) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");

        this.mEntityClass = mEntityClass;
    }

    @Override
    public String getMessage() {
        return "There is no field with annotation @PrimaryKey in " + this.mEntityClass.getSimpleName() + " class.";
    }
}