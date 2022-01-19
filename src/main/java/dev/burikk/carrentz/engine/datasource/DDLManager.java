package dev.burikk.carrentz.engine.datasource;

import dev.burikk.carrentz.engine.datasource.annotation.UniqueKeyConstraint;
import dev.burikk.carrentz.engine.datasource.enumeration.Platform;
import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.util.DataTypes;
import dev.burikk.carrentz.engine.util.Models;
import dev.burikk.carrentz.engine.util.Parameters;
import com.sun.istack.internal.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.NamingException;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * @author Muhammad Irfan
 * @since 23/08/2017 11:59
 */
@SuppressWarnings("SqlNoDataSourceInspection")
public class DDLManager extends SQLManager {
    private static final transient Logger LOGGER = LogManager.getLogger(DDLManager.class);

    DDLManager() throws SQLException, NamingException {
        super();
    }

    private DDLManager(@NotNull String mDataSourceName) throws SQLException, NamingException {
        super(mDataSourceName);
    }

    public void createTable(@NotNull Table mTable) throws SQLException {
        Parameters.requireNotNull(mTable, "mTable");

        StringBuilder mStringBuilder = new StringBuilder()
                .append("CREATE TABLE ")
                .append(mTable.getName())
                .append(" ( ")
                .append(
                        mTable
                                .getColumns()
                                .stream()
                                .map(mColumn -> {
                                    StringBuilder mInnerStringBuilder = new StringBuilder();

                                    mInnerStringBuilder
                                            .append(mColumn.getName())
                                            .append(" ")
                                            .append(DataTypes.getName(mColumn.getJDBCType(), this.mPlatform));

                                    if (mColumn.isUniqueKey()) {
                                        mInnerStringBuilder.append(" UNIQUE");
                                    }

                                    if (mColumn.isNotNull()) {
                                        mInnerStringBuilder.append(" NOT NULL");
                                    }

                                    if (mColumn.isPrimaryKey()) {
                                        mInnerStringBuilder.append(" PRIMARY KEY");
                                    }

                                    return mInnerStringBuilder.toString();
                                })
                                .collect(Collectors.joining(", "))
                )
                .append(" );");

        try (PreparedStatement mPreparedStatement = this.mConnection.prepareStatement(mStringBuilder.toString())) {
            LOGGER.debug("[Execute DDL] " + mStringBuilder.toString());

            mPreparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if (!this.mConnection.getAutoCommit()) {
                throw ex;
            } else {
                LOGGER.catching(ex);
            }
        }
    }

    public void dropTable(@NotNull String mTableName) throws SQLException {
        Parameters.requireNotNull(mTableName, "mTableName");

        String mSQLCommand = "DROP TABLE " + mTableName + ";";

        try (PreparedStatement mPreparedStatement = this.mConnection.prepareStatement(mSQLCommand)) {
            LOGGER.debug("[Execute DDL] " + mSQLCommand);

            mPreparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if (!this.mConnection.getAutoCommit()) {
                throw ex;
            } else {
                LOGGER.catching(ex);
            }
        }
    }

    public void renameTable(
            @NotNull String mOldTableName,
            @NotNull String mNewTableName
    ) throws SQLException {
        Parameters.requireNotNull(mOldTableName, "mOldTableName");
        Parameters.requireNotNull(mNewTableName, "mNewTableName");

        String mSQLCommand;
        if (Platform.SQLSERVER == this.mPlatform) {
            mSQLCommand = "EXEC sp_rename '" + mOldTableName + "', '" + mNewTableName + "';";
        } else {
            mSQLCommand = "ALTER TABLE " + mOldTableName + " RENAME TO " + mNewTableName + ";";
        }

        try (PreparedStatement mPreparedStatement = this.mConnection.prepareStatement(mSQLCommand)) {
            LOGGER.debug("[Execute DDL] " + mSQLCommand);

            mPreparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if (!this.mConnection.getAutoCommit()) {
                throw ex;
            } else {
                LOGGER.catching(ex);
            }
        }
    }

    public void addColumn(
            @NotNull String mTableName,
            @NotNull Column mColumn
    ) throws SQLException {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mColumn, "mColumn");

        StringBuilder mStringBuilder = new StringBuilder()
                .append("ALTER TABLE ")
                .append(mTableName)
                .append(" ADD");

        if (Platform.SQLSERVER != this.mPlatform) {
            mStringBuilder.append(" COLUMN ");
        }

        mStringBuilder
                .append(mColumn.getName())
                .append(" ")
                .append(DataTypes.getName(mColumn.getJDBCType(), this.mPlatform, mColumn.getMaxLength()));

        if (mColumn.isNotNull()) {
            mStringBuilder.append(" NOT NULL");
        }

        if (Platform.POSTGRESQL == this.mPlatform) {
            if (StringUtils.isNotBlank(mColumn.getDefaultValue())) {
                mStringBuilder
                        .append(" DEFAULT ")
                        .append(mColumn.getDefaultValue());
            }

            if (StringUtils.isNotBlank(mColumn.getTableReference())) {
                mStringBuilder
                        .append(" REFERENCES ")
                        .append(mColumn.getTableReference());

                if (StringUtils.isNotBlank(mColumn.getColumnReference())) {
                    mStringBuilder
                            .append(" ( ")
                            .append(mColumn.getColumnReference())
                            .append(" )");
                }
            }
        }

        mStringBuilder.append(";");

        try (PreparedStatement mPreparedStatement = this.mConnection.prepareStatement(mStringBuilder.toString())) {
            LOGGER.debug("[Execute DDL] " + mStringBuilder.toString());

            mPreparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if (!this.mConnection.getAutoCommit()) {
                throw ex;
            } else {
                LOGGER.catching(ex);
            }
        }
    }

    public void dropColumn(
            @NotNull String mTableName,
            @NotNull String mColumnName
    ) throws SQLException {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mColumnName, "mColumnName");

        String mSQLCommand = "ALTER TABLE " + mTableName + " DROP COLUMN " + mColumnName + ";";

        try (PreparedStatement mPreparedStatement = this.mConnection.prepareStatement(mSQLCommand)) {
            LOGGER.debug("[Execute DDL] " + mSQLCommand);

            mPreparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if (!this.mConnection.getAutoCommit()) {
                throw ex;
            } else {
                LOGGER.catching(ex);
            }
        }
    }

    public void renameColumn(
            @NotNull String mTableName,
            @NotNull String mOldColumnName,
            @NotNull String mNewColumnName,
            JDBCType mJDBCType
    ) throws SQLException {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mOldColumnName, "mOldColumnName");
        Parameters.requireNotNull(mNewColumnName, "mNewColumnName");
        Parameters.requireNotNull(Platform.MYSQL == this.mPlatform, mJDBCType, "mJDBCType");

        String mSQLCommand;
        switch (this.mPlatform) {
            case MYSQL:
                mSQLCommand = "ALTER TABLE " + mTableName + " CHANGE COLUMN " + mOldColumnName + " " + mNewColumnName + " " + DataTypes.getName(mJDBCType, this.mPlatform);
                break;
            case SQLSERVER:
                mSQLCommand = "EXEC sp_rename '" + mTableName + "." + mOldColumnName + "', '" + mNewColumnName + "', 'COLUMN'";
                break;
            default:
                mSQLCommand = "ALTER TABLE " + mTableName + " RENAME COLUMN " + mOldColumnName + " TO " + mNewColumnName + ";";
                break;
        }

        try (PreparedStatement mPreparedStatement = this.mConnection.prepareStatement(mSQLCommand)) {
            LOGGER.debug("[Execute DDL] " + mSQLCommand);

            mPreparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if (!this.mConnection.getAutoCommit()) {
                throw ex;
            } else {
                LOGGER.catching(ex);
            }
        }
    }

    public void changeColumnType(
            @NotNull String mTableName,
            @NotNull Column mColumn
    ) throws SQLException {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mColumn, "mColumn");

        String mSQLCommand = "ALTER TABLE " + mTableName + " ALTER COLUMN " + mColumn.getName() + " TYPE " + DataTypes.getName(mColumn.getJDBCType(), this.mPlatform, mColumn.getMaxLength()) + ";";

        try (PreparedStatement mPreparedStatement = this.mConnection.prepareStatement(mSQLCommand)) {
            LOGGER.debug("[Execute DDL] " + mSQLCommand);

            mPreparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if (!this.mConnection.getAutoCommit()) {
                throw ex;
            } else {
                LOGGER.catching(ex);
            }
        }
    }

    public void createTable(@NotNull String mTableName) throws SQLException {
        Parameters.requireNotNull(mTableName, "mTableName");

        String mSQLCommand = "CREATE TABLE IF NOT EXISTS " + mTableName + " ();";

        try (PreparedStatement mPreparedStatement = this.mConnection.prepareStatement(mSQLCommand)) {
            LOGGER.debug("[Execute DDL] " + mSQLCommand);

            mPreparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if (!this.mConnection.getAutoCommit()) {
                throw ex;
            } else {
                LOGGER.catching(ex);
            }
        }
    }

    public void markNullable(
            @NotNull String mTableName,
            @NotNull String mColumnName
    ) throws SQLException {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mColumnName, "mColumnName");

        String mSQLCommand = "ALTER TABLE " + mTableName + " ALTER COLUMN " + mColumnName + " DROP NOT NULL;";

        try (PreparedStatement mPreparedStatement = this.mConnection.prepareStatement(mSQLCommand)) {
            LOGGER.debug("[Execute DDL] " + mSQLCommand);

            mPreparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if (!this.mConnection.getAutoCommit()) {
                throw ex;
            } else {
                LOGGER.catching(ex);
            }
        }
    }

    public void markNotNull(
            @NotNull String mTableName,
            @NotNull String mColumnName
    ) throws SQLException {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mColumnName, "mColumnName");

        String mSQLCommand = "ALTER TABLE " + mTableName + " ALTER COLUMN " + mColumnName + " SET NOT NULL;";

        try (PreparedStatement mPreparedStatement = this.mConnection.prepareStatement(mSQLCommand)) {
            LOGGER.debug("[Execute DDL] " + mSQLCommand);

            mPreparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if (!this.mConnection.getAutoCommit()) {
                throw ex;
            } else {
                LOGGER.catching(ex);
            }
        }
    }

    public void dropConstraint(
            @NotNull String mTableName,
            @NotNull String mConstraintName
    ) throws SQLException {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mConstraintName, "mConstraintName");

        String mSQLCommand = "ALTER TABLE " + mTableName + " DROP CONSTRAINT " + mConstraintName + ";";

        try (PreparedStatement mPreparedStatement = this.mConnection.prepareStatement(mSQLCommand)) {
            LOGGER.debug("[Execute DDL] " + mSQLCommand);

            mPreparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if (!this.mConnection.getAutoCommit()) {
                throw ex;
            } else {
                LOGGER.catching(ex);
            }
        }
    }

    public void setPrimaryKey(
            @NotNull String mTableName,
            @NotNull Constraint mConstraint
    ) throws SQLException {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mConstraint, "mConstraint");

        StringBuilder mStringBuilder = new StringBuilder()
                .append("ALTER TABLE ")
                .append(mTableName)
                .append(" ADD CONSTRAINT ")
                .append(mConstraint.getName())
                .append(" PRIMARY KEY ( ")
                .append(String.join(", ", mConstraint.getColumnNames()))
                .append(" );");

        try (PreparedStatement mPreparedStatement = this.mConnection.prepareStatement(mStringBuilder.toString())) {
            LOGGER.debug("[Execute DDL] " + mStringBuilder);

            mPreparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if (!this.mConnection.getAutoCommit()) {
                throw ex;
            } else {
                LOGGER.catching(ex);
            }
        }
    }

    public void setUniqueKey(
            @NotNull String mTableName,
            @NotNull Constraint mConstraint
    ) throws SQLException {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mConstraint, "mConstraint");

        StringBuilder mStringBuilder = new StringBuilder();

        if (mConstraint.getMatcherMap().isEmpty()) {
            mStringBuilder
                    .append("ALTER TABLE ")
                    .append(mTableName)
                    .append(" ADD CONSTRAINT ")
                    .append(mConstraint.getName())
                    .append(" UNIQUE ( ")
                    .append(String.join(", ", mConstraint.getColumnNames()))
                    .append(" )");

            Class<? extends Entity> entityClass = Models.getEntityClass(mTableName);

            if (entityClass != null) {
                UniqueKeyConstraint mUniqueKeyConstraint = Models.getUniqueKeyConstraint(entityClass, mConstraint.getName());

                if (mUniqueKeyConstraint != null) {
                    if (mUniqueKeyConstraint.deferrable()) {
                        mStringBuilder
                                .append(" ")
                                .append("DEFERRABLE INITIALLY IMMEDIATE;");
                    } else {
                        mStringBuilder.append(";");
                    }
                } else {
                    mStringBuilder.append(";");
                }
            } else {
                mStringBuilder.append(";");
            }
        } else {
            mStringBuilder
                    .append("CREATE UNIQUE INDEX ")
                    .append(mConstraint.getName())
                    .append(" ON ")
                    .append(mTableName)
                    .append(" USING btree ( ")
                    .append(String.join(", ", mConstraint.getColumnNames()))
                    .append(" ) WHERE ")
                    .append(
                            mConstraint.getMatcherMap().entrySet()
                                    .stream()
                                    .map(mEntry -> mEntry.getValue()
                                            .stream()
                                            .map(mMatcher -> {
                                                StringBuilder mInnerStringBuilder = new StringBuilder();

                                                mInnerStringBuilder
                                                        .append(mEntry.getKey().getName());

                                                switch (mMatcher.getOperator()) {
                                                    case EQUAL:
                                                        mInnerStringBuilder
                                                                .append(" = ")
                                                                .append(mMatcher.getValues().get(0));
                                                        break;
                                                    case NOT_EQUAL:
                                                        mInnerStringBuilder
                                                                .append(" <> ")
                                                                .append(mMatcher.getValues().get(0));
                                                        break;
                                                    case LESS_THAN:
                                                        mInnerStringBuilder
                                                                .append(" < ")
                                                                .append(mMatcher.getValues().get(0));
                                                        break;
                                                    case LESS_THAN_OR_EQUAL_TO:
                                                        mInnerStringBuilder
                                                                .append(" <= ")
                                                                .append(mMatcher.getValues().get(0));
                                                        break;
                                                    case GREATER_THAN:
                                                        mInnerStringBuilder
                                                                .append(" > ")
                                                                .append(mMatcher.getValues().get(0));
                                                        break;
                                                    case GREATER_THAN_OR_EQUAL_TO:
                                                        mInnerStringBuilder
                                                                .append(" >= ")
                                                                .append(mMatcher.getValues().get(0));
                                                        break;
                                                    case IN:
                                                        mInnerStringBuilder
                                                                .append(" IN ( ")
                                                                .append(String.join(", ", mMatcher.getValues()))
                                                                .append(" )");
                                                        break;
                                                    case NOT_IN:
                                                        mInnerStringBuilder
                                                                .append(" NOT IN ( ")
                                                                .append(String.join(", ", mMatcher.getValues()))
                                                                .append(" )");
                                                        break;
                                                    case IS_NOT_NULL:
                                                        mInnerStringBuilder.append(" IS NOT NULL");
                                                        break;
                                                    case IS_NULL:
                                                        mInnerStringBuilder.append(" IS NULL");
                                                        break;
                                                }

                                                return mInnerStringBuilder.toString();
                                            })
                                            .collect(Collectors.joining(" AND ")))
                                    .collect(Collectors.joining(" AND "))
                    )
                    .append(";");
        }

        try (PreparedStatement mPreparedStatement = this.mConnection.prepareStatement(mStringBuilder.toString())) {
            LOGGER.debug("[Execute DDL] " + mStringBuilder);

            mPreparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if (!this.mConnection.getAutoCommit()) {
                throw ex;
            } else {
                LOGGER.catching(ex);
            }
        }
    }

    public void setDefaultValue(
            @NotNull String mTableName,
            @NotNull String mColumnName,
            @NotNull String mDefaultValue
    ) throws SQLException {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mColumnName, "mColumnName");
        Parameters.requireNotNull(mDefaultValue, "mDefaultValue");

        String mSQLCommand = "ALTER TABLE " + mTableName + " ALTER COLUMN " + mColumnName + " SET DEFAULT " + mDefaultValue + ";";

        try (PreparedStatement mPreparedStatement = this.mConnection.prepareStatement(mSQLCommand)) {
            LOGGER.debug("[Execute DDL] " + mSQLCommand);

            mPreparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if (!this.mConnection.getAutoCommit()) {
                throw ex;
            } else {
                LOGGER.catching(ex);
            }
        }
    }

    public void setTableReference(
            @NotNull String mTableName,
            @NotNull String mColumnName,
            @NotNull String mColumnReference,
            @NotNull String mTableReference
    ) throws SQLException {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mColumnName, "mColumnName");
        Parameters.requireNotNull(mColumnReference, "mColumnReference");
        Parameters.requireNotNull(mTableReference, "mTableReference");

        String mSQLCommand = "ALTER TABLE " + mTableName + " ADD FOREIGN KEY ( " + mColumnName + " ) REFERENCES " + mTableReference + " ( " + mColumnReference + " );";

        try (PreparedStatement mPreparedStatement = this.mConnection.prepareStatement(mSQLCommand)) {
            LOGGER.debug("[Execute DDL] " + mSQLCommand);

            mPreparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if (!this.mConnection.getAutoCommit()) {
                throw ex;
            } else {
                LOGGER.catching(ex);
            }
        }
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public String createTableScript(@NotNull Table mTable) {
        Parameters.requireNotNull(mTable, "mTable");

        StringBuilder mStringBuilder = new StringBuilder()
                .append("CREATE TABLE ")
                .append(mTable.getName())
                .append(" ( ")
                .append(
                        mTable
                                .getColumns()
                                .stream()
                                .map(mColumn -> {
                                    StringBuilder mInnerStringBuilder = new StringBuilder();

                                    mInnerStringBuilder
                                            .append(mColumn.getName())
                                            .append(" ")
                                            .append(DataTypes.getName(mColumn.getJDBCType(), this.mPlatform));

                                    if (mColumn.isUniqueKey()) {
                                        mInnerStringBuilder.append(" UNIQUE");
                                    }

                                    if (mColumn.isNotNull()) {
                                        mInnerStringBuilder.append(" NOT NULL");
                                    }

                                    if (mColumn.isPrimaryKey()) {
                                        mInnerStringBuilder.append(" PRIMARY KEY");
                                    }

                                    return mInnerStringBuilder.toString();
                                })
                                .collect(Collectors.joining(", "))
                )
                .append(" );");

        return mStringBuilder.toString();
    }

    public String dropTableScript(@NotNull String mTableName) {
        Parameters.requireNotNull(mTableName, "mTableName");

        return "DROP TABLE " + mTableName + ";";
    }

    public String renameTableScript(
            @NotNull String mOldTableName,
            @NotNull String mNewTableName
    ) {
        Parameters.requireNotNull(mOldTableName, "mOldTableName");
        Parameters.requireNotNull(mNewTableName, "mNewTableName");

        String mSQLCommand;
        if (Platform.SQLSERVER == this.mPlatform) {
            mSQLCommand = "EXEC sp_rename '" + mOldTableName + "', '" + mNewTableName + "';";
        } else {
            mSQLCommand = "ALTER TABLE " + mOldTableName + " RENAME TO " + mNewTableName + ";";
        }

        return mSQLCommand;
    }

    public String addColumnScript(
            @NotNull String mTableName,
            @NotNull Column mColumn
    ) {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mColumn, "mColumn");

        StringBuilder mStringBuilder = new StringBuilder()
                .append("ALTER TABLE ")
                .append(mTableName)
                .append(" ADD");

        if (Platform.SQLSERVER != this.mPlatform) {
            mStringBuilder.append(" COLUMN ");
        }

        mStringBuilder
                .append(mColumn.getName())
                .append(" ")
                .append(DataTypes.getName(mColumn.getJDBCType(), this.mPlatform, mColumn.getMaxLength()));

        if (mColumn.isNotNull()) {
            mStringBuilder.append(" NOT NULL");
        }

        if (Platform.POSTGRESQL == this.mPlatform) {
            if (StringUtils.isNotBlank(mColumn.getDefaultValue())) {
                mStringBuilder
                        .append(" DEFAULT ")
                        .append(mColumn.getDefaultValue());
            }

            if (StringUtils.isNotBlank(mColumn.getTableReference())) {
                mStringBuilder
                        .append(" REFERENCES ")
                        .append(mColumn.getTableReference());

                if (StringUtils.isNotBlank(mColumn.getColumnReference())) {
                    mStringBuilder
                            .append(" ( ")
                            .append(mColumn.getColumnReference())
                            .append(" )");
                }
            }
        }

        mStringBuilder.append(";");

        return mStringBuilder.toString();
    }

    String dropColumnScript(
            @NotNull String mTableName,
            @NotNull String mColumnName
    ) {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mColumnName, "mColumnName");

        return "ALTER TABLE " + mTableName + " DROP COLUMN " + mColumnName + ";";
    }

    public String renameColumnScript(
            @NotNull String mTableName,
            @NotNull String mOldColumnName,
            @NotNull String mNewColumnName,
            JDBCType mJDBCType
    ) {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mOldColumnName, "mOldColumnName");
        Parameters.requireNotNull(mNewColumnName, "mNewColumnName");
        Parameters.requireNotNull(false, mJDBCType, "mJDBCType");

        String mSQLCommand;
        switch (this.mPlatform) {
            case MYSQL:
                mSQLCommand = "ALTER TABLE " + mTableName + " CHANGE COLUMN " + mOldColumnName + " " + mNewColumnName + " " + DataTypes.getName(mJDBCType, Platform.MYSQL);
                break;
            case SQLSERVER:
                mSQLCommand = "EXEC sp_rename '" + mTableName + "." + mOldColumnName + "', '" + mNewColumnName + "', 'COLUMN'";
                break;
            default:
                mSQLCommand = "ALTER TABLE " + mTableName + " RENAME COLUMN " + mOldColumnName + " TO " + mNewColumnName + ";";
                break;
        }

        return mSQLCommand;
    }

    public String changeColumnTypeScript(
            @NotNull String mTableName,
            @NotNull Column mColumn
    ) {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mColumn, "mColumn");

        return "ALTER TABLE " + mTableName + " ALTER COLUMN " + mColumn.getName() + " TYPE " + DataTypes.getName(mColumn.getJDBCType(), this.mPlatform, mColumn.getMaxLength()) + ";";
    }

    public String createTableScript(@NotNull String mTableName) {
        Parameters.requireNotNull(mTableName, "mTableName");

        return "CREATE TABLE IF NOT EXISTS " + mTableName + " ();";
    }

    public String markNullableScript(
            @NotNull String mTableName,
            @NotNull String mColumnName
    ) {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mColumnName, "mColumnName");

        return "ALTER TABLE " + mTableName + " ALTER COLUMN " + mColumnName + " DROP NOT NULL;";
    }

    public String markNotNullScript(
            @NotNull String mTableName,
            @NotNull String mColumnName
    ) {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mColumnName, "mColumnName");

        return "ALTER TABLE " + mTableName + " ALTER COLUMN " + mColumnName + " SET NOT NULL;";
    }

    public String dropConstraintScript(
            @NotNull String mTableName,
            @NotNull String mConstraintName
    ) {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mConstraintName, "mConstraintName");

        return "ALTER TABLE " + mTableName + " DROP CONSTRAINT " + mConstraintName + ";";
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public String setPrimaryKeyScript(
            @NotNull String mTableName,
            @NotNull Constraint mConstraint
    ) {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mConstraint, "mConstraint");

        StringBuilder mStringBuilder = new StringBuilder()
                .append("ALTER TABLE ")
                .append(mTableName)
                .append(" ADD CONSTRAINT ")
                .append(mConstraint.getName())
                .append(" PRIMARY KEY ( ")
                .append(String.join(", ", mConstraint.getColumnNames()))
                .append(" );");

        return mStringBuilder.toString();
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public String setUniqueKeyScript(
            @NotNull String mTableName,
            @NotNull Constraint mConstraint
    ) {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mConstraint, "mConstraint");

        StringBuilder mStringBuilder = new StringBuilder();

        mStringBuilder
                .append("ALTER TABLE ")
                .append(mTableName)
                .append(" ADD CONSTRAINT ")
                .append(mConstraint.getName())
                .append(" UNIQUE ( ")
                .append(String.join(", ", mConstraint.getColumnNames()))
                .append(" )");

        Class<? extends Entity> entityClass = Models.getEntityClass(mTableName);

        if (entityClass != null) {
            UniqueKeyConstraint mUniqueKeyConstraint = Models.getUniqueKeyConstraint(entityClass, mConstraint.getName());

            if (mUniqueKeyConstraint != null) {
                if (mUniqueKeyConstraint.deferrable()) {
                    mStringBuilder
                            .append(" ")
                            .append("DEFERRABLE INITIALLY IMMEDIATE;");
                } else {
                    mStringBuilder.append(";");
                }
            } else {
                mStringBuilder.append(";");
            }
        } else {
            mStringBuilder.append(";");
        }

        return mStringBuilder.toString();
    }

    public String setDefaultValueScript(
            @NotNull String mTableName,
            @NotNull String mColumnName,
            @NotNull String mDefaultValue
    ) {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mColumnName, "mColumnName");
        Parameters.requireNotNull(mDefaultValue, "mDefaultValue");

        return "ALTER TABLE " + mTableName + " ALTER COLUMN " + mColumnName + " SET DEFAULT " + mDefaultValue + ";";
    }

    public String setTableReferenceScript(
            @NotNull String mTableName,
            @NotNull String mColumnName,
            @NotNull String mColumnReference,
            @NotNull String mTableReference
    ) {
        Parameters.requireNotNull(mTableName, "mTableName");
        Parameters.requireNotNull(mColumnName, "mColumnName");
        Parameters.requireNotNull(mColumnReference, "mColumnReference");
        Parameters.requireNotNull(mTableReference, "mTableReference");

        return "ALTER TABLE " + mTableName + " ADD FOREIGN KEY ( " + mColumnName + " ) REFERENCES " + mTableReference + ";";
    }
}