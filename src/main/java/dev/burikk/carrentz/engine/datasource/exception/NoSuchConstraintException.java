package dev.burikk.carrentz.engine.datasource.exception;

import dev.burikk.carrentz.engine.util.Parameters;
import com.sun.istack.internal.NotNull;

/**
 * @author Muhammad Irfan
 * @since 20/06/2017 14:38
 */
public class NoSuchConstraintException extends RuntimeException {
    private final String mTableName;
    private final String mConstraintName;

    public NoSuchConstraintException(
            @NotNull String mTableName,
            @NotNull String mConstraintName
    ) {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mConstraintName, "mConstraintName");

        this.mTableName = mTableName;
        this.mConstraintName = mConstraintName;
    }

    @Override
    public String getMessage() {
        return "Constraint with name " + this.mConstraintName + " cannot be found at table " + this.mTableName + ".";
    }
}