package dev.burikk.carrentz.engine.datasource.exception;

import dev.burikk.carrentz.engine.util.Parameters;
import com.sun.istack.internal.NotNull;

/**
 * @author Muhammad Irfan
 * @since 21/06/2017 14:58
 */
public class NoSuchTableException extends RuntimeException {
    private final String mTableName;

    public NoSuchTableException(@NotNull String mTableName) {
        Parameters.requireNotNull(mTableName, "mTableName");

        this.mTableName = mTableName;
    }

    @Override
    public String getMessage() {
        return "Table with name " + this.mTableName + " is not exist.";
    }
}