package dev.burikk.carrentz.engine.datasource.exception;

import dev.burikk.carrentz.engine.util.Parameters;
import javax.validation.constraints.NotNull;

import java.sql.JDBCType;

/**
 * @author Muhammad Irfan
 * @since 20/06/2017 10:10
 */
public class UndefinedJDBCTypeMappingException extends RuntimeException {
    private final JDBCType mJDBCType;

    public UndefinedJDBCTypeMappingException(@NotNull JDBCType mJDBCType) {
        Parameters.requireNotNull(mJDBCType, "mJDBCType");

        this.mJDBCType = mJDBCType;
    }

    @Override
    public String getMessage() {
        return "Undefined JDBC type mapping for " + this.mJDBCType.getName() + ".";
    }
}