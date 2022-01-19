package dev.burikk.carrentz.engine.datasource;

import dev.burikk.carrentz.engine.datasource.annotation.MarkEncryptedColumn;
import dev.burikk.carrentz.engine.datasource.exception.NoSuchMarkEncryptedColumnException;
import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.util.Parameters;
import com.sun.istack.internal.NotNull;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.sql.JDBCType;

/**
 * @author Muhammad Irfan
 * @since 24/11/2017 15:36
 */
public class EncryptedColumn extends Column {
    private EncryptedColumn(
            @NotNull String mName,
            @NotNull Field mField,
            boolean mNotNull
    ) {
        super(mName, JDBCType.VARCHAR, mField, 256, mNotNull, null, null, null, false, null, false, null, null);
    }

    public static EncryptedColumn valueOf(
            @NotNull Class<? extends Entity> mEntityClass,
            @NotNull Field mField
    ) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");
        Parameters.requireNotNull(mField, "mField");

        if (!mField.isAnnotationPresent(MarkEncryptedColumn.class)) {
            throw new NoSuchMarkEncryptedColumnException(mEntityClass, mField);
        }

        MarkEncryptedColumn mMarkEncryptedColumn = mField.getAnnotation(MarkEncryptedColumn.class);

        String mColumnName;
        if (StringUtils.isNotBlank(mMarkEncryptedColumn.value())) {
            mColumnName = mMarkEncryptedColumn.value();
        } else {
            mColumnName = mField.getName();
        }

        return new EncryptedColumn(mColumnName, mField, mMarkEncryptedColumn.isNotNull());
    }
}