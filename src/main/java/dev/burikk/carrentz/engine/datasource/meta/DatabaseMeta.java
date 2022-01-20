package dev.burikk.carrentz.engine.datasource.meta;

import dev.burikk.carrentz.engine.common.Constant;
import dev.burikk.carrentz.engine.datasource.exception.NoSuchTableException;
import dev.burikk.carrentz.engine.util.Parameters;
import javax.validation.constraints.NotNull;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Muhammad Irfan
 * @since 23/08/2017 11:00
 */
public class DatabaseMeta {
    public static final List<TableMeta> TABLE_METAS;

    static {
        TABLE_METAS = new ArrayList<>();
    }

    public static boolean isTableExist(@NotNull String mTableName) {
        Parameters.requireNotNull(mTableName, "mTableName");

        return TABLE_METAS
                .stream()
                .anyMatch(mTableMeta -> Objects.equals(mTableName, mTableMeta.getTableName()));
    }

    public static TableMeta getTableMeta(@NotNull String mTableName) {
        Parameters.requireNotNull(mTableName, "mTableName");

        return TABLE_METAS
                .stream()
                .filter(mTableMeta -> Objects.equals(mTableName, mTableMeta.getTableName()))
                .findFirst()
                .orElseThrow(() -> new NoSuchTableException(mTableName));
    }

    public static void scan(@NotNull Connection mConnection) {
        Parameters.requireNotNull(mConnection, "mConnection");

        try (ResultSet mRSTable = mConnection.getMetaData().getTables(null, Constant.Database.SCHEMA_NAME, null, new String[]{"TABLE"})) {
            while (mRSTable.next()) {
                String mTableName = mRSTable.getString("TABLE_NAME");

                ConstraintMeta mPKConstraintMeta = null;
                List<ConstraintMeta> mUKConstraintMetas = new ArrayList<>();
                List<ColumnMeta> mColumnMetas = new ArrayList<>();
                try (ResultSet mRSColumn = mConnection.getMetaData().getColumns(null, Constant.Database.SCHEMA_NAME, mTableName, null)) {
                    while (mRSColumn.next()) {
                        String mColumnName = mRSColumn.getString("COLUMN_NAME");
                        String mDefaultValue = mRSColumn.getString("COLUMN_DEF");
                        JDBCType mJDBCType = JDBCType.valueOf(mRSColumn.getInt("DATA_TYPE"));
                        Integer mMaxLength = mRSColumn.getInt("COLUMN_SIZE");
                        Boolean mNotNull = !"YES".equalsIgnoreCase(mRSColumn.getString("IS_NULLABLE"));

                        String mColumnReference = null;
                        String mTableReference = null;
                        try (ResultSet mRSImportedKey = mConnection.getMetaData().getImportedKeys(null, Constant.Database.SCHEMA_NAME, mTableName)) {
                            while (mRSImportedKey.next()) {
                                if (Objects.equals(mColumnName, mRSImportedKey.getString("FKCOLUMN_NAME"))) {
                                    mColumnReference = mRSImportedKey.getString("PKCOLUMN_NAME");
                                    mTableReference = mRSImportedKey.getString("PKTABLE_NAME");
                                    break;
                                }
                            }
                        }

                        boolean mPrimaryKey = false;
                        String mPrimaryKeyConstraintName = null;
                        try (ResultSet mRSPrimaryKey = mConnection.getMetaData().getPrimaryKeys(null, Constant.Database.SCHEMA_NAME, mTableName)) {
                            while (mRSPrimaryKey.next()) {
                                if (Objects.equals(mColumnName, mRSPrimaryKey.getString("COLUMN_NAME"))) {
                                    mPrimaryKey = true;
                                    mPrimaryKeyConstraintName = mRSPrimaryKey.getString("PK_NAME");

                                    if (mPKConstraintMeta == null) {
                                        mPKConstraintMeta = new ConstraintMeta(mPrimaryKeyConstraintName, mColumnName);
                                    } else {
                                        mPKConstraintMeta.getColumnNames().add(mColumnName);
                                    }
                                    break;
                                }
                            }
                        }

                        boolean mUniqueKey = false;
                        String mUniqueKeyConstraintName = null;
                        try (ResultSet mIndexInfoRS = mConnection.getMetaData().getIndexInfo(null, Constant.Database.SCHEMA_NAME, mTableName, true, false)) {
                            while (mIndexInfoRS.next()) {
                                if (Objects.equals(mColumnName, mIndexInfoRS.getString("COLUMN_NAME"))) {
                                    mUniqueKey = !mIndexInfoRS.getBoolean("NON_UNIQUE");

                                    if (mUniqueKey) {
                                        mUniqueKeyConstraintName = mIndexInfoRS.getString("INDEX_NAME");

                                        if (Objects.equals(mUniqueKeyConstraintName, mPrimaryKeyConstraintName)) {
                                            continue;
                                        }

                                        boolean[] mFound = {false};

                                        String mFinalUniqueKeyConstraintName = mUniqueKeyConstraintName;

                                        mUKConstraintMetas
                                                .stream()
                                                .filter(mUKConstraintMeta -> Objects.equals(mFinalUniqueKeyConstraintName, mUKConstraintMeta.getName()))
                                                .findFirst()
                                                .ifPresent(mUKConstraintMeta -> {
                                                    mUKConstraintMeta.getColumnNames().add(mColumnName);
                                                    mFound[0] = true;
                                                });

                                        if (!mFound[0]) {
                                            ConstraintMeta mUKConstraintMeta = new ConstraintMeta(mUniqueKeyConstraintName, mColumnName);
                                            mUKConstraintMetas.add(mUKConstraintMeta);
                                        }
                                    }
                                }
                            }
                        }

                        ColumnMeta mColumnMeta = new ColumnMeta(mColumnName, mJDBCType, mNotNull, mMaxLength, mDefaultValue, mColumnReference, mTableReference, mPrimaryKey, mPrimaryKeyConstraintName, mUniqueKey, mUniqueKeyConstraintName);

                        mColumnMetas.add(mColumnMeta);
                    }
                }

                TableMeta mTableMeta = new TableMeta(mTableName, mColumnMetas, mPKConstraintMeta, mUKConstraintMetas);

                TABLE_METAS.add(mTableMeta);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Fail to scan database meta data.", ex);
        }
    }
}