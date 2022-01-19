package dev.burikk.carrentz.engine.datasource.meta;

import dev.burikk.carrentz.engine.datasource.exception.NoSuchColumnException;
import dev.burikk.carrentz.engine.datasource.exception.NoSuchConstraintException;
import dev.burikk.carrentz.engine.util.Parameters;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author Muhammad Irfan
 * @since 23/08/2017 10:52
 */
public class TableMeta {
    //<editor-fold desc="Property">
    private String mName;
    private List<ColumnMeta> mColumnMetas;
    private ConstraintMeta mPrimaryKeyConstraintMeta;
    private List<ConstraintMeta> mUniqueKeyConstraintMetas;
    //</editor-fold>

    TableMeta(
            @NotNull String mName,
            @Nullable List<ColumnMeta> mColumnMetas,
            @Nullable ConstraintMeta mPrimaryKeyConstraintMeta,
            @Nullable List<ConstraintMeta> mUniqueKeyConstraintMetas
    ) {
        Parameters.requireNotNull(mName, "mName");

        this.mName = mName;
        this.mColumnMetas = mColumnMetas;
        this.mPrimaryKeyConstraintMeta = mPrimaryKeyConstraintMeta;
        this.mUniqueKeyConstraintMetas = mUniqueKeyConstraintMetas;
    }

    public boolean isColumnExist(@NotNull String mColumnName) {
        if (StringUtils.isBlank(mColumnName)) {
            throw new IllegalArgumentException("mColumnName cannot be empty.");
        }

        return this.mColumnMetas
                .stream()
                .anyMatch(mColumnMeta -> Objects.equals(mColumnName, mColumnMeta.getName()));
    }

    public ColumnMeta getColumnMeta(@NotNull String mColumnName) {
        Parameters.requireNotNull(mColumnName, "mColumnName");

        return this.mColumnMetas
                .stream()
                .filter(mColumnMeta -> Objects.equals(mColumnName, mColumnMeta.getName()))
                .findFirst()
                .orElseThrow(() -> new NoSuchColumnException(this.mName, mColumnName));
    }

    public ConstraintMeta getUniqueKeyConstraintMeta(@NotNull String mConstraintName) {
        Parameters.requireNotNull(mConstraintName, "mConstraintName");

        return this.mUniqueKeyConstraintMetas
                .stream()
                .filter(mUniqueKeyConstraintMeta -> Objects.equals(mConstraintName, mUniqueKeyConstraintMeta.getName()))
                .findFirst()
                .orElseThrow(() -> new NoSuchConstraintException(this.mName, mConstraintName));
    }

    //<editor-fold desc="Getter">
    public String getTableName() {
        return mName;
    }

    public List<ColumnMeta> getColumnMetas() {
        return mColumnMetas;
    }

    public ConstraintMeta getPrimaryKeyConstraintMeta() {
        return mPrimaryKeyConstraintMeta;
    }

    public List<ConstraintMeta> getUniqueKeyConstraintMetas() {
        return mUniqueKeyConstraintMetas;
    }
    //</editor-fold>
}