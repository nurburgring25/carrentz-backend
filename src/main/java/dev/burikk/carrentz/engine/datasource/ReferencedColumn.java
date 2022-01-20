package dev.burikk.carrentz.engine.datasource;

import dev.burikk.carrentz.engine.datasource.annotation.MarkReferencedColumn;
import dev.burikk.carrentz.engine.datasource.exception.NoSuchMarkReferencedColumnException;
import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.util.Parameters;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import java.lang.reflect.Field;
import java.sql.JDBCType;

/**
 * @author Muhammad Irfan
 * @since 23/08/2017 11:17
 */
@SuppressWarnings("WeakerAccess")
public class ReferencedColumn extends BaseColumn {
    //<editor-fold desc="Property">
    private final int mReferenceID;
    private final String mAliasName;
    //</editor-fold>

    private ReferencedColumn(
            @NotNull String mName,
            @NotNull Field mField,
            @NotNull JDBCType mJDBCType,
            int mReferenceID,
            @Null String mAliasName
    ) {
        super(mName, mJDBCType, mField);

        this.mReferenceID = mReferenceID;
        this.mAliasName = mAliasName;
    }

    public static ReferencedColumn valueOf(
            @NotNull Class<? extends Entity> mEntityClass,
            @NotNull Field mField
    ) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");
        Parameters.requireNotNull(mField, "mField");

        if (!mField.isAnnotationPresent(MarkReferencedColumn.class)) {
            throw new NoSuchMarkReferencedColumnException(mEntityClass, mField);
        }

        MarkReferencedColumn mMarkReferencedColumn = mField.getAnnotation(MarkReferencedColumn.class);

        return new ReferencedColumn(mMarkReferencedColumn.value(), mField, mMarkReferencedColumn.jdbcType(), mMarkReferencedColumn.referenceID(), mMarkReferencedColumn.alias());
    }

    //<editor-fold desc="Getter">
    public int getReferenceID() {
        return mReferenceID;
    }

    public String getAliasName() {
        return mAliasName;
    }
    //</editor-fold>
}