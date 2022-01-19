package dev.burikk.carrentz.engine.datasource.meta;

import dev.burikk.carrentz.engine.util.Parameters;
import com.sun.istack.internal.NotNull;

import java.sql.JDBCType;

/**
 * @author Muhammad Irfan
 * @since 23/08/2017 10:47
 */
public class ColumnMeta {
    //<editor-fold desc="Property">
    private final String mName;
    private final JDBCType mJDBCType;
    private final int mMaxLength;
    private final boolean mNotNull;
    private final String mDefaultValue;
    private final String mColumnReference;
    private final String mTableReference;
    private final Boolean mPrimaryKey;
    private final String mPrimaryKeyConstraintName;
    private final Boolean mUniqueKey;
    private final String mUniqueKeyConstraintName;
    //</editor-fold>

    ColumnMeta(
            @NotNull String mName,
            @NotNull JDBCType mJDBCType,
            @NotNull Boolean mNotNull,
            Integer mMaxLength,
            String mDefaultValue,
            String mColumnReference,
            String mTableReference,
            Boolean mPrimaryKey,
            String mPrimaryKeyConstraintName,
            Boolean mUniqueKey,
            String mUniqueKeyConstraintName
    ) {
        Parameters.requireNotNull(mName, "mName");
        Parameters.requireNotNull(mJDBCType, "mJDBCType");

        this.mName = mName;
        this.mJDBCType = mJDBCType;
        this.mMaxLength = mMaxLength;
        this.mNotNull = mNotNull;
        this.mDefaultValue = mDefaultValue;
        this.mColumnReference = mColumnReference;
        this.mTableReference = mTableReference;
        this.mPrimaryKey = mPrimaryKey;
        this.mPrimaryKeyConstraintName = mPrimaryKeyConstraintName;
        this.mUniqueKey = mUniqueKey;
        this.mUniqueKeyConstraintName = mUniqueKeyConstraintName;
    }

    //<editor-fold desc="Getter">
    public String getName() {
        return mName;
    }

    public JDBCType getJDBCType() {
        return mJDBCType;
    }

    public int getMaxLength() {
        return mMaxLength;
    }

    public boolean isNotNull() {
        return mNotNull;
    }

    public String getDefaultValue() {
        return mDefaultValue;
    }

    public String getColumnReference() {
        return mColumnReference;
    }

    public String getTableReference() {
        return mTableReference;
    }

    public boolean isPrimaryKey() {
        return mPrimaryKey;
    }

    public String getPrimaryKeyConstraintName() {
        return mPrimaryKeyConstraintName;
    }

    public boolean isUniqueKey() {
        return mUniqueKey;
    }

    public String getUniqueKeyConstraintName() {
        return mUniqueKeyConstraintName;
    }
    //</editor-fold>
}