package dev.burikk.carrentz.engine.datasource.exception;

import dev.burikk.carrentz.engine.util.Parameters;
import javax.validation.constraints.NotNull;

/**
 * @author Muhammad Irfan
 * @since 20/06/2017 14:30
 */
public class NoSuchColumnException extends RuntimeException {
    private final String mTableName;
    private final String mColumnName;

    public NoSuchColumnException(
            @NotNull String mTableName,
            @NotNull String mColumnName
    ) {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mColumnName, "mColumnName");

        this.mTableName = mTableName;
        this.mColumnName = mColumnName;
    }

    @Override
    public String getMessage() {
        return "Column with name " + this.mColumnName + " cannot be found at table " + this.mTableName + ".";
    }
}