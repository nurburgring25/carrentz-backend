package dev.burikk.carrentz.engine.datasource.exception;

import dev.burikk.carrentz.engine.datasource.enumeration.Platform;
import dev.burikk.carrentz.engine.util.Parameters;
import javax.validation.constraints.NotNull;

import java.sql.JDBCType;

/**
 * @author Muhammad Irfan
 * @since 20/06/2017 9:04
 */
public class UnsupportedDataTypeException extends RuntimeException {
    private final Platform mPlatform;
    private final JDBCType mJDBCType;

    public UnsupportedDataTypeException(
            @NotNull Platform mPlatform,
            @NotNull JDBCType mJDBCType
    ) {
        Parameters.requireNotNull(mPlatform, "mPlatform");
        Parameters.requireNotNull(mJDBCType, "mJDBCType");

        this.mPlatform = mPlatform;
        this.mJDBCType = mJDBCType;
    }

    @Override
    public String getMessage() {
        return this.mJDBCType.getName() + " is not supported by " + this.mPlatform.name() + " platform.";
    }
}