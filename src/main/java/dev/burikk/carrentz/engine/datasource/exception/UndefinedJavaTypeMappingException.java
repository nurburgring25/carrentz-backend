package dev.burikk.carrentz.engine.datasource.exception;

import dev.burikk.carrentz.engine.util.Parameters;
import com.sun.istack.internal.NotNull;

/**
 * @author Muhammad Irfan
 * @since 20/06/2017 10:01
 */
public class UndefinedJavaTypeMappingException extends RuntimeException {
    private final Class<?> mJavaType;

    public UndefinedJavaTypeMappingException(@NotNull Class<?> mJavaType) {
        Parameters.requireNotNull(mJavaType, "mJavaType");

        this.mJavaType = mJavaType;
    }

    @Override
    public String getMessage() {
        return "Undefined java type mapping for " + this.mJavaType.getSimpleName() + ".";
    }
}