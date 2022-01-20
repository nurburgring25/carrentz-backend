package dev.burikk.carrentz.engine.datasource;

import dev.burikk.carrentz.engine.common.LanguageManager;
import dev.burikk.carrentz.engine.common.SessionManager;
import dev.burikk.carrentz.engine.common.WynixResult;
import dev.burikk.carrentz.engine.common.WynixResults;
import dev.burikk.carrentz.engine.datasource.annotation.Reference;
import dev.burikk.carrentz.engine.datasource.annotation.UniqueKeyConstraint;
import dev.burikk.carrentz.engine.datasource.exception.NoSuchPrimaryKeyException;
import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.entity.EntityCache;
import dev.burikk.carrentz.engine.entity.EntityDesign;
import dev.burikk.carrentz.engine.entity.HashEntity;
import dev.burikk.carrentz.engine.entity.annotation.MarkAuditable;
import dev.burikk.carrentz.engine.entity.annotation.MarkDeletable;
import dev.burikk.carrentz.engine.exception.WynixException;
import dev.burikk.carrentz.engine.security.Crypt;
import dev.burikk.carrentz.engine.util.DataTypes;
import dev.burikk.carrentz.engine.util.Models;
import dev.burikk.carrentz.engine.util.Parameters;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.util.PSQLException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.naming.NamingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.burikk.carrentz.engine.common.Constant.Reflection.*;

/**
 * @author Muhammad Irfan
 * @since 22/06/2017 14:34
 */
public class DMLManager extends SQLManager {
    private static final transient Logger LOGGER = LogManager.getLogger(DMLManager.class);

    public DMLManager() throws SQLException, NamingException {
        super();
    }

    public DMLManager(@NotNull String mDataSourceName) throws SQLException, NamingException {
        super(mDataSourceName);
    }

    public void deferUniqueConstraint(String constraintName) throws SQLException {
        try (PreparedStatement mPreparedStatement = this.mConnection.prepareStatement("SET CONSTRAINTS " + constraintName + " DEFERRED;")) {
            LOGGER.debug("[Open Deferrable Unique Constraint] SET CONSTRAINTS " + constraintName + " DEFERRED;");

            mPreparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if (!this.mConnection.getAutoCommit()) {
                throw ex;
            } else {
                LOGGER.catching(ex);
            }
        }
    }

    private long insert(@NotNull Entity mEntity) throws SQLException {
        Parameters.requireNotNull(mEntity, "mEntity");

        EntityDesign mEntityDesign = EntityCache.getInstance().getEntityDesign(mEntity.getClass());

        Map<Field, Column> mColumnMap = mEntityDesign.getColumnMap();

        StringBuilder mStringBuilder = new StringBuilder()
                .append("INSERT INTO ")
                .append(mEntityDesign.getTableName())
                .append(" ( ");

        Supplier<Stream<Map.Entry<Field, Column>>> mSupplier = () -> mColumnMap
                .entrySet()
                .stream()
                .filter(mEntry -> {
                    if (!mEntity.isAuditable()) {
                        switch (mEntry.getKey().getName()) {
                            case FIELD_CREATED:
                                return false;
                            case FIELD_CREATOR:
                                return false;
                            case FIELD_MODIFIED:
                                return false;
                            case FIELD_MODIFICATOR:
                                return false;
                        }
                    }

                    Field mField = mEntry.getKey();

                    if (!mField.isAccessible()) {
                        mField.setAccessible(true);
                    }

                    try {
                        Object mObject = mField.get(mEntity);

                        if (mObject == null && !Objects.equals(mField.getName(), FIELD_MODIFIED)) {
                            return false;
                        }
                    } catch (Exception ex) {
                        LOGGER.catching(ex);
                    }

                    return true;
                });

        mStringBuilder
                .append(
                        mSupplier.get()
                                .map(mEntry -> mEntry.getValue().getName())
                                .collect(Collectors.joining(", "))
                )
                .append(" ) VALUES ( ")
                .append(String.join(", ", Collections.nCopies((int) mSupplier.get().count(), "?")))
                .append(" );");

        long mResult = 0;

        try (PreparedStatement mPreparedStatement = mConnection.prepareStatement(mStringBuilder.toString(), PreparedStatement.RETURN_GENERATED_KEYS)) {
            int[] mIndex = {1};

            mSupplier.get().forEach(mEntry -> {
                try {
                    Object mObject = DataTypes.convert(mEntry.getKey().get(mEntity));

                    if (mEntry.getValue() instanceof EncryptedColumn) {
                        mPreparedStatement.setString(mIndex[0]++, Crypt.encrypt(mObject));
                    } else {
                        if (mObject instanceof String) {
                            mPreparedStatement.setString(mIndex[0]++, (String) mObject);
                        } else {
                            mPreparedStatement.setObject(mIndex[0]++, DataTypes.convert(mObject));
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.catching(ex);
                }
            });

            LOGGER.debug("[Execute DML] " + mStringBuilder.toString());

            try {
                mPreparedStatement.executeUpdate();
            } catch (PSQLException ex) {
                if (StringUtils.equals(ex.getSQLState(), "23505")) {
                    UniqueKeyConstraint uniqueKeyConstraint = Models.getUniqueKeyConstraint(mEntity.getClass(), ex.getServerErrorMessage().getConstraint());

                    if (uniqueKeyConstraint != null && StringUtils.isNotBlank(uniqueKeyConstraint.violationMessage())) {
                        throw new WynixException(LanguageManager.retrieve(SessionManager.getInstance().getLocale(), uniqueKeyConstraint.violationMessage()));
                    } else {
                        throw ex;
                    }
                } else {
                    throw ex;
                }
            }

            SQLWarning mSQLWarning = mPreparedStatement.getWarnings();

            if (mSQLWarning != null) {
                LOGGER.error("---------------------BEGIN SQL WARNING--------------------");

                while (mSQLWarning != null) {
                    LOGGER.error(mSQLWarning.getMessage());

                    mSQLWarning = mSQLWarning.getNextWarning();
                }

                LOGGER.error("---------------------END SQL WARNING--------------------");
            }

            try {
                ResultSet mResultSet = mPreparedStatement.getGeneratedKeys();

                while (mResultSet.next()) {
                    mResult = mResultSet.getLong(mEntityDesign.getPrimaryKeyColumn().getName());
                }
            } catch (Exception ignored) {}
        }

        return mResult;
    }

    private void update(@NotNull Entity mEntity) throws IllegalAccessException, SQLException {
        Parameters.requireNotNull(mEntity, "mEntity");

        EntityDesign mEntityDesign = EntityCache.getInstance().getEntityDesign(mEntity.getClass());

        if (mEntityDesign.getPrimaryKeyColumn() == null) {
            throw new NoSuchPrimaryKeyException(mEntity.getClass());
        }

        if (!mEntityDesign.getPrimaryKeyField().isAccessible()) {
            mEntityDesign.getPrimaryKeyField().setAccessible(true);
        }

        Object mPrimaryKeyValue = DataTypes.convert(mEntityDesign.getPrimaryKeyField().get(mEntity));

        if (mPrimaryKeyValue == null) {
            throw new NullPointerException("Primary key value cannot be null.");
        }

        Supplier<Stream<Map.Entry<Field, Column>>> mSupplier = () -> mEntityDesign
                .getColumnMap()
                .entrySet()
                .stream()
                .filter(mEntry -> {
                    if (mEntry.getValue().isPrimaryKey()) {
                        return false;
                    }

                    if (!mEntity.isDeletable()) {
                        if (FIELD_DELETED.equals(mEntry.getKey().getName())) {
                            return false;
                        }
                    }

                    if (!mEntity.isAuditable()) {
                        switch (mEntry.getKey().getName()) {
                            case FIELD_CREATED:
                                return false;
                            case FIELD_CREATOR:
                                return false;
                            case FIELD_MODIFIED:
                                return false;
                            case FIELD_MODIFICATOR:
                                return false;
                        }
                    }

                    Field mField = mEntry.getKey();

                    if (!mField.isAccessible()) {
                        mField.setAccessible(true);
                    }

                    return true;
                });

        StringBuilder mStringBuilder = new StringBuilder()
                .append("UPDATE ")
                .append(mEntityDesign.getTableName())
                .append(" SET ")
                .append(
                        mSupplier.get()
                                .map(mEntry -> {
                                    Object mObject = null;
                                    try {
                                        mObject = DataTypes.convert(mEntry.getKey().get(mEntity));
                                    } catch (Exception ex) {
                                        LOGGER.catching(ex);
                                    }

                                    String mParameter = "?";
                                    if (mObject == null) {
                                        if (StringUtils.isNotBlank(mEntry.getValue().getDefaultValue())) {
                                            mParameter = "DEFAULT";
                                        }
                                    }

                                    return mEntry.getValue().getName() + " = " + mParameter;
                                })
                                .collect(Collectors.joining(", "))
                )
                .append(" WHERE ")
                .append(mEntityDesign.getPrimaryKeyColumn().getName())
                .append(" = ?;");

        try (PreparedStatement mPreparedStatement = mConnection.prepareStatement(mStringBuilder.toString())) {
            int[] mIndex = {1};

            mSupplier.get().forEach(mEntry -> {
                try {
                    Object mObject = DataTypes.convert(mEntry.getKey().get(mEntity));

                    if (mObject == null) {
                        if (StringUtils.isBlank(mEntry.getValue().getDefaultValue())) {
                            mPreparedStatement.setNull(mIndex[0]++, mEntry.getValue().getJDBCType().getVendorTypeNumber());
                        }
                    } else {
                        if (mEntry.getValue() instanceof EncryptedColumn) {
                            mPreparedStatement.setObject(mIndex[0]++, Crypt.encrypt(mObject));
                        } else {
                            if (mObject instanceof String) {
                                mPreparedStatement.setString(mIndex[0]++, (String) mObject);
                            } else {
                                mPreparedStatement.setObject(mIndex[0]++, mObject);
                            }
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.catching(ex);
                }
            });

            mPreparedStatement.setObject(mIndex[0]++, mPrimaryKeyValue);

            LOGGER.debug("[Execute DML] " + mStringBuilder.toString());

            try {
                mPreparedStatement.executeUpdate();
            } catch (PSQLException ex) {
                if (StringUtils.equals(ex.getSQLState(), "23505")) {
                    UniqueKeyConstraint uniqueKeyConstraint = Models.getUniqueKeyConstraint(mEntity.getClass(), ex.getServerErrorMessage().getConstraint());

                    if (uniqueKeyConstraint != null && StringUtils.isNotBlank(uniqueKeyConstraint.violationMessage())) {
                        throw new WynixException(LanguageManager.retrieve(SessionManager.getInstance().getLocale(), uniqueKeyConstraint.violationMessage()));
                    } else {
                        throw ex;
                    }
                }
            }

            SQLWarning mSQLWarning = mPreparedStatement.getWarnings();

            if (mSQLWarning != null) {
                LOGGER.error("---------------------BEGIN SQL WARNING--------------------");

                while (mSQLWarning != null) {
                    LOGGER.error(mSQLWarning.getMessage());

                    mSQLWarning = mSQLWarning.getNextWarning();
                }

                LOGGER.error("---------------------END SQL WARNING--------------------");
            }
        }
    }

    private void delete(@NotNull Entity mEntity) throws IllegalAccessException, SQLException {
        Parameters.requireNotNull(mEntity, "mEntity");

        EntityDesign mEntityDesign = EntityCache.getInstance().getEntityDesign(mEntity.getClass());

        if (mEntityDesign.getPrimaryKeyColumn() == null) {
            throw new NoSuchPrimaryKeyException(mEntity.getClass());
        }

        if (!mEntityDesign.getPrimaryKeyField().isAccessible()) {
            mEntityDesign.getPrimaryKeyField().setAccessible(true);
        }

        Object mPrimaryKeyValue = DataTypes.convert(mEntityDesign.getPrimaryKeyField().get(mEntity));

        if (mPrimaryKeyValue == null) {
            throw new NullPointerException("Primary key value cannot be null.");
        }

        StringBuilder mStringBuilder;

        if (mEntityDesign.getDeletable()) {
            mStringBuilder = new StringBuilder()
                    .append("UPDATE ")
                    .append(mEntityDesign.getTableName())
                    .append(" SET deleted = TRUE")
                    .append(" WHERE ")
                    .append(mEntityDesign.getPrimaryKeyColumn().getName())
                    .append(" = ?;");
        } else {
            mStringBuilder = new StringBuilder()
                    .append("DELETE FROM ")
                    .append(mEntityDesign.getTableName())
                    .append(" WHERE ")
                    .append(mEntityDesign.getPrimaryKeyColumn().getName())
                    .append(" = ?;");
        }


        try (PreparedStatement mPreparedStatement = mConnection.prepareStatement(mStringBuilder.toString())) {
            mPreparedStatement.setObject(1, mPrimaryKeyValue);

            LOGGER.debug("[Execute DML] " + mStringBuilder.toString());

            mPreparedStatement.executeUpdate();

            SQLWarning mSQLWarning = mPreparedStatement.getWarnings();

            if (mSQLWarning != null) {
                LOGGER.error("---------------------BEGIN SQL WARNING--------------------");

                while (mSQLWarning != null) {
                    LOGGER.error(mSQLWarning.getMessage());

                    mSQLWarning = mSQLWarning.getNextWarning();
                }

                LOGGER.error("---------------------END SQL WARNING--------------------");
            }
        }
    }

    public <T extends Entity> void store(@NotNull WynixResults<T> mWynixResults) throws SQLException, IllegalAccessException, InstantiationException, NamingException {
        Parameters.requireNotNull(mWynixResults, "mWynixResults");

        for (T mEntity : mWynixResults) {
            this.store(mEntity);
        }
    }

    public <T extends Entity> long store(@NotNull T mEntity) throws SQLException, IllegalAccessException, NamingException, InstantiationException {
        Parameters.requireNotNull(mEntity, "mEntity");

        if (mEntity.isNew()) {
            mEntity.doNew();
            return insert(mEntity);
        } else if (mEntity.isUpdate()) {
            mEntity.doUpdate();
            update(mEntity);
        } else if (mEntity.isDelete()) {
            mEntity.doDelete();
            delete(mEntity);
        }

        return 0;
    }

    public void execute(
            @NotNull String query,
            @Null Object... parameters
    ) throws SQLException {
        Parameters.requireNotNull(query, "query");

        try (PreparedStatement preparedStatement = this.mConnection.prepareStatement(query)) {
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    Object object = parameters[i];

                    if (object instanceof String) {
                        preparedStatement.setString(i + 1, (String) object);
                    } else {
                        preparedStatement.setObject(i + 1, DataTypes.convert(object));
                    }
                }
            }

            LOGGER.debug("[Execute DML] " + query);

            preparedStatement.execute();
        }
    }

    public static <T extends Entity> long storeImmediately(@NotNull T mEntity) throws SQLException, NamingException, IllegalAccessException, InstantiationException {
        try(DMLManager mDMLManager = new DMLManager()) {
            return mDMLManager.store(mEntity);
        }
    }

    public static void executeImmediately(
            @NotNull String query,
            @Null Object... parameters
    ) throws NamingException, SQLException {
        try (DMLManager dmlManager = new DMLManager()) {
            dmlManager.execute(query, parameters);
        }
    }

    @SafeVarargs
    @SuppressWarnings("StringBufferReplaceableByString")
    public static <T extends Entity> WynixResults<T> getWynixResults(
            @Null String mDataSourceName,
            @NotNull Class<T> mEntityClass,
            @NotNull Class<? extends Annotation>... mAnnotationClasses
    ) throws SQLException, InstantiationException, IllegalAccessException, NamingException {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");

        EntityDesign mEntityDesign = EntityCache.getInstance().getEntityDesign(mEntityClass);

        Supplier<Stream<Map.Entry<Field, BaseColumn>>> mSupplier;

        if (mAnnotationClasses.length>0) {
            mSupplier = () -> mEntityDesign
                    .getMap()
                    .entrySet()
                    .stream()
                    .filter(mEntry -> {
                        if (!mEntityClass.isAnnotationPresent(MarkDeletable.class)) {
                            if (Objects.equals(FIELD_DELETED, mEntry.getKey().getName())) {
                                return false;
                            }
                        }

                        if (!mEntityClass.isAnnotationPresent(MarkAuditable.class)) {
                            switch (mEntry.getKey().getName()) {
                                case FIELD_CREATED:
                                    return false;
                                case FIELD_CREATOR:
                                    return false;
                                case FIELD_MODIFIED:
                                    return false;
                                case FIELD_MODIFICATOR:
                                    return false;
                            }
                        }

                        return true;
                    })
                    .filter(mEntry -> {
                        boolean mPassed = false;

                        for (Class<? extends Annotation> mAnnotationClass : mAnnotationClasses) {
                            if (mEntry.getKey().isAnnotationPresent(mAnnotationClass)) {
                                mPassed = true;
                                break;
                            }
                        }

                        return mPassed;
                    })
                    .sorted((o1, o2) -> (o1 instanceof Column) ? -1 : (o2 instanceof ReferencedColumn) ? 0 : 1);
        } else {
            mSupplier = () -> mEntityDesign
                    .getMap()
                    .entrySet()
                    .stream()
                    .filter(mEntry -> {
                        if (!mEntityClass.isAnnotationPresent(MarkDeletable.class)) {
                            if (Objects.equals(FIELD_DELETED, mEntry.getKey().getName())) {
                                return false;
                            }
                        }

                        if (!mEntityClass.isAnnotationPresent(MarkAuditable.class)) {
                            switch (mEntry.getKey().getName()) {
                                case FIELD_CREATED:
                                    return false;
                                case FIELD_CREATOR:
                                    return false;
                                case FIELD_MODIFIED:
                                    return false;
                                case FIELD_MODIFICATOR:
                                    return false;
                            }
                        }

                        return true;
                    })
                    .sorted((o1, o2) -> (o1 instanceof Column) ? -1 : (o2 instanceof ReferencedColumn) ? 0 : 1);
        }

        List<Reference> mReferences = Models.getReferences(mEntityClass);

        StringBuilder mStringBuilder = new StringBuilder()
                .append("SELECT ")
                .append(
                        mSupplier
                                .get()
                                .map(mEntry -> {
                                    BaseColumn mBaseColumn = mEntry.getValue();

                                    StringBuilder mInnerStringBuilder = new StringBuilder();

                                    if (mBaseColumn instanceof ReferencedColumn) {
                                        ReferencedColumn mReferencedColumn = (ReferencedColumn) mBaseColumn;

                                        Reference mReference = Models.getReference(mReferences, mReferencedColumn.getReferenceID());

                                        if (StringUtils.isNotBlank(mReference.targetAliasTable())) {
                                            mInnerStringBuilder.append(mReference.targetAliasTable());
                                        } else {
                                            mInnerStringBuilder.append(mReference.targetTable());
                                        }

                                        mInnerStringBuilder
                                                .append(".")
                                                .append(mReferencedColumn.getName());

                                        if (StringUtils.isNotBlank(mReferencedColumn.getAliasName())) {
                                            mInnerStringBuilder
                                                    .append(" AS ")
                                                    .append(mReferencedColumn.getAliasName());
                                        }
                                    } else {
                                        Column mColumn = (Column) mBaseColumn;

                                        mInnerStringBuilder
                                                .append(mEntityDesign.getTableName())
                                                .append(".")
                                                .append(mColumn.getName());
                                    }

                                    return mInnerStringBuilder.toString();
                                })
                                .collect(Collectors.joining(", "))
                )
                .append(" FROM ")
                .append(mEntityDesign.getTableName())
                .append(" ")
                .append(
                        mReferences
                                .stream()
                                .map(mReference -> {
                                    StringBuilder mInnerStringBuilder = new StringBuilder()
                                            .append(mReference.joinType().getText())
                                            .append(" ")
                                            .append(mReference.targetTable());

                                    String mTargetTableName;
                                    if (StringUtils.isNotBlank(mReference.targetAliasTable())) {
                                        mInnerStringBuilder
                                                .append(" AS ")
                                                .append(mReference.targetAliasTable());

                                        mTargetTableName = mReference.targetAliasTable();
                                    } else {
                                        mTargetTableName = mReference.targetTable();
                                    }

                                    mInnerStringBuilder
                                            .append(" ON ")
                                            .append(mTargetTableName)
                                            .append(".")
                                            .append(mReference.targetColumn())
                                            .append(" = ");

                                    if (StringUtils.isNotBlank(mReference.sourceTable())) {
                                        mInnerStringBuilder.append(mReference.sourceTable());
                                    } else {
                                        mInnerStringBuilder.append(mEntityDesign.getTableName());
                                    }

                                    mInnerStringBuilder
                                            .append(".")
                                            .append(mReference.sourceColumn());

                                    return mInnerStringBuilder.toString();
                                })
                                .collect(Collectors.joining(" "))
                )
                .append(";");

        return getWynixResultsFromQuery(mDataSourceName, mStringBuilder.toString(), mEntityClass);
    }

    @SafeVarargs
    @SuppressWarnings("StringBufferReplaceableByString")
    public static <T extends Entity> WynixResults<T> getWynixResults(
            @NotNull Class<T> mClass,
            @NotNull Class<? extends Annotation>... mAnnotationClasses
    ) throws SQLException, NamingException, InstantiationException, IllegalAccessException {
        return getWynixResults(null, mClass, mAnnotationClasses);
    }

    @SuppressWarnings("unchecked")
    public static <T extends WynixResult> WynixResults<T> getWynixResultsFromQuery(
            @Null String mDataSourceName,
            @NotNull String mQuery,
            @NotNull Class<T> mClass,
            @Null Object... mParameters
    ) throws SQLException, NamingException, IllegalAccessException, InstantiationException {
        Parameters.requireNotNull(mQuery, "mQuery");
        Parameters.requireNotNull(mClass, "mClass");

        WynixResults<T> mWynixResults = new WynixResults<>();

        try (
                DMLManager mDMLManager = StringUtils.isNotBlank(mDataSourceName) ? new DMLManager(mDataSourceName) : new DMLManager();
                PreparedStatement mPreparedStatement = mDMLManager.mConnection.prepareStatement(mQuery)
        ) {
            if (mParameters != null) {
                for (int i = 0; i < mParameters.length; i++) {
                    Object object = mParameters[i];

                    if (object instanceof String) {
                        mPreparedStatement.setString(i + 1, (String) object);
                    } else {
                        mPreparedStatement.setObject(i + 1, DataTypes.convert(object));
                    }
                }
            }

            LOGGER.debug("[Execute DML] " + mQuery);

            try (ResultSet mResultSet = mPreparedStatement.executeQuery()) {
                if (Objects.equals(mClass, HashEntity.class)) {
                    while (mResultSet.next()) {
                        HashEntity mHashEntity = new HashEntity();

                        ResultSetMetaData mResultSetMetaData = mResultSet.getMetaData();

                        for (int i = 1; i <= mResultSetMetaData.getColumnCount(); i++) {
                            mHashEntity.add(mResultSetMetaData.getColumnLabel(i), DataTypes.convert(mResultSet.getObject(i)));
                        }

                        mWynixResults.add((T) mHashEntity);
                    }
                } else if (Objects.equals(mClass, LOVItem.class)) {
                    while (mResultSet.next()) {
                        LOVItem mLOVItem = new LOVItem();

                        mLOVItem.setIdentity(DataTypes.convert(mResultSet.getObject(1)));
                        mLOVItem.setDescription(mResultSet.getString(2));

                        ResultSetMetaData mResultSetMetaData = mResultSet.getMetaData();

                        for (int i = 3; i <= mResultSetMetaData.getColumnCount(); i++) {
                            mLOVItem.getOptions().put(mResultSetMetaData.getColumnLabel(i), DataTypes.convert(mResultSet.getObject(i)));
                        }

                        mWynixResults.add((T) mLOVItem);
                    }
                } else {
                    EntityDesign mEntityDesign = EntityCache.getInstance().getEntityDesign((Class<? extends Entity>) mClass);

                    while (mResultSet.next()) {
                        T mEntity = mClass.newInstance();

                        mEntityDesign
                                .getMap()
                                .forEach((mField, mColumn) -> {
                                    try {
                                        if (!mField.isAccessible()) {
                                            mField.setAccessible(true);
                                        }

                                        try {
                                            String mColumnName;

                                            if (mColumn instanceof ReferencedColumn) {
                                                ReferencedColumn mReferencedColumn = (ReferencedColumn) mColumn;

                                                mColumnName = StringUtils.isNotBlank(mReferencedColumn.getAliasName()) ? mReferencedColumn.getAliasName() : mReferencedColumn.getName();
                                            } else {
                                                mColumnName = mColumn.getName();
                                            }

                                            Object mObject = mResultSet.getObject(mColumnName);

                                            if (mObject != null) {
                                                if (mColumn instanceof EncryptedColumn) {
                                                    mField.set(mEntity, Crypt.decrypt(mObject.toString(), mField.getType()));
                                                } else {
                                                    mField.set(mEntity, DataTypes.convert(mObject));
                                                }
                                            }
                                        } catch (SQLException ignored) {
                                        }
                                    } catch (IllegalAccessException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException ex) {
                                        LOGGER.catching(ex);
                                    }
                                });

                        mWynixResults.add(mEntity);
                    }
                }
            }
        }

        return mWynixResults;
    }

    public static <T extends WynixResult> WynixResults<T> getWynixResultsFromQuery(
            @NotNull String mQuery,
            @NotNull Class<T> mClass,
            @Null Object... mParameters
    ) throws SQLException, InstantiationException, IllegalAccessException, NamingException {
        return getWynixResultsFromQuery(null, mQuery, mClass, mParameters);
    }

    public static <T extends WynixResult> WynixResults<T> getWynixResultsFromQuery(
            @NotNull String mQuery,
            @NotNull Class<T> mClass
    ) throws SQLException, InstantiationException, IllegalAccessException, NamingException {
        return getWynixResultsFromQuery(null, mQuery, mClass);
    }

    @SuppressWarnings("unchecked")
    public static <T extends WynixResult> T getWynixResultFromQuery(
            @Null String mDataSourceName,
            @NotNull String mQuery,
            @NotNull Class<T> mClass,
            @Null Object... mParameters
    ) throws SQLException, NamingException, IllegalAccessException, InstantiationException {
        Parameters.requireNotNull(mQuery, "mQuery");
        Parameters.requireNotNull(mClass, "mClass");

        try (
                DMLManager mDMLManager = StringUtils.isNotBlank(mDataSourceName) ? new DMLManager(mDataSourceName) : new DMLManager();
                PreparedStatement mPreparedStatement = mDMLManager.mConnection.prepareStatement(mQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
        ) {
            if (mParameters != null) {
                for (int i = 0; i < mParameters.length; i++) {
                    Object object = mParameters[i];

                    if (object instanceof String) {
                        mPreparedStatement.setString(i + 1, (String) object);
                    } else {
                        mPreparedStatement.setObject(i + 1, DataTypes.convert(object));
                    }
                }
            }

            LOGGER.debug("[Execute DML] " + mQuery);

            try (ResultSet mResultSet = mPreparedStatement.executeQuery()) {
                if (mResultSet.first()) {
                    if (Objects.equals(mClass, HashEntity.class)) {
                        HashEntity mHashEntity = new HashEntity();

                        ResultSetMetaData mResultSetMetaData = mResultSet.getMetaData();

                        for (int i = 1; i <= mResultSetMetaData.getColumnCount(); i++) {
                            mHashEntity.add(mResultSetMetaData.getColumnLabel(i), DataTypes.convert(mResultSet.getObject(i)));
                        }

                        return (T) mHashEntity;
                    } else if (Objects.equals(mClass, LOVItem.class)) {
                        LOVItem mLOVItem = new LOVItem();

                        mLOVItem.setIdentity(mResultSet.getObject(1));
                        mLOVItem.setDescription(mResultSet.getString(2));

                        return (T) mLOVItem;
                    } else {
                        T mEntity = mClass.newInstance();

                        EntityDesign mEntityDesign = EntityCache.getInstance().getEntityDesign((Class<? extends Entity>) mClass);

                        mEntityDesign
                                .getMap()
                                .forEach((mField, mColumn) -> {
                                    try {
                                        if (!mField.isAccessible()) {
                                            mField.setAccessible(true);
                                        }

                                        try {
                                            String mColumnName;

                                            if (mColumn instanceof ReferencedColumn) {
                                                ReferencedColumn mReferencedColumn = (ReferencedColumn) mColumn;

                                                mColumnName = StringUtils.isNotBlank(mReferencedColumn.getAliasName()) ? mReferencedColumn.getAliasName() : mReferencedColumn.getName();
                                            } else {
                                                mColumnName = mColumn.getName();
                                            }

                                            Object mObject = mResultSet.getObject(mColumnName);

                                            if (mObject != null) {
                                                if (mColumn instanceof EncryptedColumn) {
                                                    mField.set(mEntity, Crypt.decrypt(mObject.toString(), mField.getType()));
                                                } else {
                                                    mField.set(mEntity, DataTypes.convert(mObject));
                                                }
                                            }
                                        } catch (SQLException ignored) {}
                                    } catch (IllegalAccessException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException ex) {
                                        LOGGER.catching(ex);
                                    }
                                });

                        return mEntity;
                    }
                }
            }
        }

        return null;
    }

    public static <T extends WynixResult> T getWynixResultFromQuery(
            @NotNull String mQuery,
            @NotNull Class<T> mClass,
            @Null Object... mParameters
    ) throws SQLException, InstantiationException, IllegalAccessException, NamingException {
        return getWynixResultFromQuery(null, mQuery, mClass, mParameters);
    }

    public static <T extends WynixResult> T getWynixResultFromQuery(
            @NotNull String mQuery,
            @NotNull Class<T> mClass
    ) throws SQLException, InstantiationException, IllegalAccessException, NamingException {
        return getWynixResultFromQuery(null, mQuery, mClass);
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    @SafeVarargs
    public static <T extends Entity> T getEntity(
            @Null String mDataSourceName,
            @NotNull Class<T> mClass,
            @NotNull Object mID,
            @NotNull Class<? extends Annotation>... mAnnotationClasses
    ) throws SQLException, InstantiationException, IllegalAccessException, NamingException {
        Parameters.requireNotNull(mClass, "mClass");

        EntityDesign mEntityDesign = EntityCache.getInstance().getEntityDesign(mClass);

        Supplier<Stream<Map.Entry<Field, BaseColumn>>> mSupplier;

        if (mAnnotationClasses.length>0) {
            mSupplier = () -> mEntityDesign
                    .getMap()
                    .entrySet()
                    .stream()
                    .filter(mEntry -> {
                        if (!mClass.isAnnotationPresent(MarkDeletable.class)) {
                            if (Objects.equals(FIELD_DELETED, mEntry.getKey().getName())) {
                                return false;
                            }
                        }

                        if (!mClass.isAnnotationPresent(MarkAuditable.class)) {
                            switch (mEntry.getKey().getName()) {
                                case FIELD_CREATED:
                                    return false;
                                case FIELD_CREATOR:
                                    return false;
                                case FIELD_MODIFIED:
                                    return false;
                                case FIELD_MODIFICATOR:
                                    return false;
                            }
                        }

                        return true;
                    })
                    .filter(mEntry -> {
                        boolean mPassed = false;

                        for (Class<? extends Annotation> mAnnotationClass : mAnnotationClasses) {
                            if (mEntry.getKey().isAnnotationPresent(mAnnotationClass)) {
                                mPassed = true;
                                break;
                            }
                        }

                        return mPassed;
                    })
                    .sorted((o1, o2) -> (o1 instanceof Column) ? -1 : (o2 instanceof ReferencedColumn) ? 0 : 1);
        } else {
            mSupplier = () -> mEntityDesign
                    .getMap()
                    .entrySet()
                    .stream()
                    .filter(mEntry -> {
                        if (!mClass.isAnnotationPresent(MarkDeletable.class)) {
                            if (Objects.equals(FIELD_DELETED, mEntry.getKey().getName())) {
                                return false;
                            }
                        }

                        if (!mClass.isAnnotationPresent(MarkAuditable.class)) {
                            switch (mEntry.getKey().getName()) {
                                case FIELD_CREATED:
                                    return false;
                                case FIELD_CREATOR:
                                    return false;
                                case FIELD_MODIFIED:
                                    return false;
                                case FIELD_MODIFICATOR:
                                    return false;
                            }
                        }

                        return true;
                    })
                    .sorted((o1, o2) -> (o1 instanceof Column) ? -1 : (o2 instanceof ReferencedColumn) ? 0 : 1);
        }

        List<Reference> mReferences = Models.getReferences(mClass);

        StringBuilder mStringBuilder = new StringBuilder()
                .append("SELECT ")
                .append(
                        mSupplier
                                .get()
                                .map(mEntry -> {
                                    BaseColumn mBaseColumn = mEntry.getValue();

                                    StringBuilder mInnerStringBuilder = new StringBuilder();

                                    if (mBaseColumn instanceof ReferencedColumn) {
                                        ReferencedColumn mReferencedColumn = (ReferencedColumn) mBaseColumn;

                                        Reference mReference = Models.getReference(mReferences, mReferencedColumn.getReferenceID());

                                        if (StringUtils.isNotBlank(mReference.targetAliasTable())) {
                                            mInnerStringBuilder.append(mReference.targetAliasTable());
                                        } else {
                                            mInnerStringBuilder.append(mReference.targetTable());
                                        }

                                        mInnerStringBuilder
                                                .append(".")
                                                .append(mReferencedColumn.getName());

                                        if (StringUtils.isNotBlank(mReferencedColumn.getAliasName())) {
                                            mInnerStringBuilder
                                                    .append(" AS ")
                                                    .append(mReferencedColumn.getAliasName());
                                        }
                                    } else {
                                        Column mColumn = (Column) mBaseColumn;

                                        mInnerStringBuilder
                                                .append(mEntityDesign.getTableName())
                                                .append(".")
                                                .append(mColumn.getName());
                                    }

                                    return mInnerStringBuilder.toString();
                                })
                                .collect(Collectors.joining(", "))
                )
                .append(" FROM ")
                .append(mEntityDesign.getTableName())
                .append(" ")
                .append(
                        mReferences
                                .stream()
                                .map(mReference -> {
                                    StringBuilder mInnerStringBuilder = new StringBuilder()
                                            .append(mReference.joinType().getText())
                                            .append(" ")
                                            .append(mReference.targetTable());

                                    String mTargetTableName;
                                    if (StringUtils.isNotBlank(mReference.targetAliasTable())) {
                                        mInnerStringBuilder
                                                .append(" AS ")
                                                .append(mReference.targetAliasTable());

                                        mTargetTableName = mReference.targetAliasTable();
                                    } else {
                                        mTargetTableName = mReference.targetTable();
                                    }

                                    mInnerStringBuilder
                                            .append(" ON ")
                                            .append(mTargetTableName)
                                            .append(".")
                                            .append(mReference.targetColumn())
                                            .append(" = ");

                                    if (StringUtils.isNotBlank(mReference.sourceTable())) {
                                        mInnerStringBuilder.append(mReference.sourceTable());
                                    } else {
                                        mInnerStringBuilder.append(mEntityDesign.getTableName());
                                    }

                                    mInnerStringBuilder
                                            .append(".")
                                            .append(mReference.sourceColumn());

                                    return mInnerStringBuilder.toString();
                                })
                                .collect(Collectors.joining(" "))
                )
                .append(" WHERE ")
                .append(mEntityDesign.getPrimaryKeyColumn().getName())
                .append(" = ?;");

        return getWynixResultFromQuery(mDataSourceName, mStringBuilder.toString(), mClass, mID);
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    @SafeVarargs
    public static <T extends Entity> T getEntity(
            @NotNull Class<T> mClass,
            @NotNull Object mID,
            @NotNull Class<? extends Annotation>... mAnnotationClasses
    ) throws SQLException, NamingException, InstantiationException, IllegalAccessException {
        Parameters.requireNotNull(mClass, "mClass");

        EntityDesign mEntityDesign = EntityCache.getInstance().getEntityDesign(mClass);

        Supplier<Stream<Map.Entry<Field, BaseColumn>>> mSupplier;

        if (mAnnotationClasses.length>0) {
            mSupplier = () -> mEntityDesign
                    .getMap()
                    .entrySet()
                    .stream()
                    .filter(mEntry -> {
                        if (!mClass.isAnnotationPresent(MarkDeletable.class)) {
                            if (Objects.equals(FIELD_DELETED, mEntry.getKey().getName())) {
                                return false;
                            }
                        }

                        if (!mClass.isAnnotationPresent(MarkAuditable.class)) {
                            switch (mEntry.getKey().getName()) {
                                case FIELD_CREATED:
                                    return false;
                                case FIELD_CREATOR:
                                    return false;
                                case FIELD_MODIFIED:
                                    return false;
                                case FIELD_MODIFICATOR:
                                    return false;
                            }
                        }

                        return true;
                    })
                    .filter(mEntry -> {
                        boolean mPassed = false;

                        for (Class<? extends Annotation> mAnnotationClass : mAnnotationClasses) {
                            if (mEntry.getKey().isAnnotationPresent(mAnnotationClass)) {
                                mPassed = true;
                                break;
                            }
                        }

                        return mPassed;
                    })
                    .sorted((o1, o2) -> (o1 instanceof Column) ? -1 : (o2 instanceof ReferencedColumn) ? 0 : 1);
        } else {
            mSupplier = () -> mEntityDesign
                    .getMap()
                    .entrySet()
                    .stream()
                    .filter(mEntry -> {
                        if (!mClass.isAnnotationPresent(MarkDeletable.class)) {
                            if (Objects.equals(FIELD_DELETED, mEntry.getKey().getName())) {
                                return false;
                            }
                        }

                        if (!mClass.isAnnotationPresent(MarkAuditable.class)) {
                            switch (mEntry.getKey().getName()) {
                                case FIELD_CREATED:
                                    return false;
                                case FIELD_CREATOR:
                                    return false;
                                case FIELD_MODIFIED:
                                    return false;
                                case FIELD_MODIFICATOR:
                                    return false;
                            }
                        }

                        return true;
                    })
                    .sorted((o1, o2) -> (o1 instanceof Column) ? -1 : (o2 instanceof ReferencedColumn) ? 0 : 1);
        }

        List<Reference> mReferences = Models.getReferences(mClass);

        StringBuilder mStringBuilder = new StringBuilder()
                .append("SELECT ")
                .append(
                        mSupplier
                                .get()
                                .map(mEntry -> {
                                    BaseColumn mBaseColumn = mEntry.getValue();

                                    StringBuilder mInnerStringBuilder = new StringBuilder();

                                    if (mBaseColumn instanceof ReferencedColumn) {
                                        ReferencedColumn mReferencedColumn = (ReferencedColumn) mBaseColumn;

                                        Reference mReference = Models.getReference(mReferences, mReferencedColumn.getReferenceID());

                                        if (StringUtils.isNotBlank(mReference.targetAliasTable())) {
                                            mInnerStringBuilder.append(mReference.targetAliasTable());
                                        } else {
                                            mInnerStringBuilder.append(mReference.targetTable());
                                        }

                                        mInnerStringBuilder
                                                .append(".")
                                                .append(mReferencedColumn.getName());

                                        if (StringUtils.isNotBlank(mReferencedColumn.getAliasName())) {
                                            mInnerStringBuilder
                                                    .append(" AS ")
                                                    .append(mReferencedColumn.getAliasName());
                                        }
                                    } else {
                                        Column mColumn = (Column) mBaseColumn;

                                        mInnerStringBuilder
                                                .append(mEntityDesign.getTableName())
                                                .append(".")
                                                .append(mColumn.getName());
                                    }

                                    return mInnerStringBuilder.toString();
                                })
                                .collect(Collectors.joining(", "))
                )
                .append(" FROM ")
                .append(mEntityDesign.getTableName())
                .append(" ")
                .append(
                        mReferences
                                .stream()
                                .map(mReference -> {
                                    StringBuilder mInnerStringBuilder = new StringBuilder()
                                            .append(mReference.joinType().getText())
                                            .append(" ")
                                            .append(mReference.targetTable());

                                    String mTargetTableName;
                                    if (StringUtils.isNotBlank(mReference.targetAliasTable())) {
                                        mInnerStringBuilder
                                                .append(" AS ")
                                                .append(mReference.targetAliasTable());

                                        mTargetTableName = mReference.targetAliasTable();
                                    } else {
                                        mTargetTableName = mReference.targetTable();
                                    }

                                    mInnerStringBuilder
                                            .append(" ON ")
                                            .append(mTargetTableName)
                                            .append(".")
                                            .append(mReference.targetColumn())
                                            .append(" = ");

                                    if (StringUtils.isNotBlank(mReference.sourceTable())) {
                                        mInnerStringBuilder.append(mReference.sourceTable());
                                    } else {
                                        mInnerStringBuilder.append(mEntityDesign.getTableName());
                                    }

                                    mInnerStringBuilder
                                            .append(".")
                                            .append(mReference.sourceColumn());

                                    return mInnerStringBuilder.toString();
                                })
                                .collect(Collectors.joining(" "))
                )
                .append(" WHERE ")
                .append(mEntityDesign.getTableName())
                .append(".")
                .append(mEntityDesign.getPrimaryKeyColumn().getName())
                .append(" = ?;");

        return getWynixResultFromQuery(mStringBuilder.toString(), mClass, mID);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getObjectsFromQuery(
            @Null String mDataSourceName,
            @NotNull String mQuery,
            @Null Object... mParameters
    ) throws SQLException, NamingException {
        Parameters.requireNotNull(mQuery, "mQuery");

        List<T> mList = new ArrayList<>();

        try (
                DMLManager mDMLManager = StringUtils.isNotBlank(mDataSourceName) ? new DMLManager(mDataSourceName) : new DMLManager();
                PreparedStatement mPreparedStatement = mDMLManager.mConnection.prepareStatement(mQuery)
        ) {
            if (mParameters != null) {
                for (int i = 0; i < mParameters.length; i++) {
                    Object object = mParameters[i];

                    if (object instanceof String) {
                        mPreparedStatement.setString(i + 1, (String) object);
                    } else {
                        mPreparedStatement.setObject(i + 1, DataTypes.convert(object));
                    }
                }
            }

            LOGGER.debug("[Execute DML] " + mQuery);

            try (ResultSet mResultSet = mPreparedStatement.executeQuery()) {
                while (mResultSet.next()) {
                    mList.add((T) DataTypes.convert(mResultSet.getObject(1)));
                }
            }
        }

        return mList;
    }

    public static <T> List<T> getObjectsFromQuery(
            @NotNull String mQuery,
            @Null Object... mParameters
    ) throws SQLException, NamingException {
        return getObjectsFromQuery(null, mQuery, mParameters);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> getEncryptedObjectsFromQuery(
            @Null String mDataSourceName,
            @NotNull String mQuery,
            @NotNull Class<T> mType,
            @Null Object... mParameters
    ) throws SQLException, NamingException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Parameters.requireNotNull(mQuery, "mQuery");
        Parameters.requireNotNull(mType, "mType");

        List<T> mList = new ArrayList<>();

        try (
                DMLManager mDMLManager = StringUtils.isNotBlank(mDataSourceName) ? new DMLManager(mDataSourceName) : new DMLManager();
                PreparedStatement mPreparedStatement = mDMLManager.mConnection.prepareStatement(mQuery)
        ) {
            if (mParameters != null) {
                for (int i = 0; i < mParameters.length; i++) {
                    Object object = mParameters[i];

                    if (object instanceof String) {
                        mPreparedStatement.setString(i + 1, (String) object);
                    } else {
                        mPreparedStatement.setObject(i + 1, DataTypes.convert(object));
                    }
                }
            }

            LOGGER.debug("[Execute DML] " + mQuery);

            try (ResultSet mResultSet = mPreparedStatement.executeQuery()) {
                while (mResultSet.next()) {
                    mList.add(Crypt.decrypt(mResultSet.getString(1), mType));
                }
            }
        }

        return mList;
    }

    public static <T> List<T> getEncryptedObjectsFromQuery(
            @NotNull String mQuery,
            @NotNull Class<T> mType,
            @Null Object... mParameters
    ) throws NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, SQLException, NamingException, InvalidKeyException {
        return getEncryptedObjectsFromQuery(null, mQuery, mType, mParameters);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getObjectFromQuery(
            @Null String mDataSourceName,
            @NotNull String mQuery,
            @Null Object... mParameters
    ) throws SQLException, NamingException {
        Parameters.requireNotNull(mQuery, "mQuery");

        try (
                DMLManager mDMLManager = StringUtils.isNotBlank(mDataSourceName) ? new DMLManager(mDataSourceName) : new DMLManager();
                PreparedStatement mPreparedStatement = mDMLManager.mConnection.prepareStatement(mQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
        ) {
            if (mParameters != null) {
                for (int i = 0; i < mParameters.length; i++) {
                    Object object = mParameters[i];

                    if (object instanceof String) {
                        mPreparedStatement.setString(i + 1, (String) object);
                    } else {
                        mPreparedStatement.setObject(i + 1, DataTypes.convert(object));
                    }
                }
            }

            LOGGER.debug("[Execute DML] " + mQuery);

            try (ResultSet mResultSet = mPreparedStatement.executeQuery()) {
                if (mResultSet.first()) {
                    return (T) DataTypes.convert(mResultSet.getObject(1));
                }
            }
        }

        return null;
    }

    public static <T> T getObjectFromQuery(
            @NotNull String mQuery,
            @Null Object... mParameters
    ) throws SQLException, NamingException {
        return getObjectFromQuery(null, mQuery, mParameters);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getEncryptedObjectFromQuery(
            @Null String mDataSourceName,
            @NotNull String mQuery,
            @NotNull Class<T> mType,
            @Null Object... mParameters
    ) throws SQLException, NamingException, NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Parameters.requireNotNull(mQuery, "mQuery");
        Parameters.requireNotNull(mType, "mType");

        try (
                DMLManager mDMLManager = StringUtils.isNotBlank(mDataSourceName) ? new DMLManager(mDataSourceName) : new DMLManager();
                PreparedStatement mPreparedStatement = mDMLManager.mConnection.prepareStatement(mQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
        ) {
            if (mParameters != null) {
                for (int i = 0; i < mParameters.length; i++) {
                    Object object = mParameters[i];

                    if (object instanceof String) {
                        mPreparedStatement.setString(i + 1, (String) object);
                    } else {
                        mPreparedStatement.setObject(i + 1, DataTypes.convert(object));
                    }
                }
            }

            LOGGER.debug("[Execute DML] " + mQuery);

            try (ResultSet mResultSet = mPreparedStatement.executeQuery()) {
                if (mResultSet.first()) {
                    return Crypt.decrypt(mResultSet.getString(1), mType);
                }
            }
        }

        return null;
    }

    public static <T> T getEncryptedObjectFromQuery(
            @NotNull String mQuery,
            @NotNull Class<T> mType,
            @Null Object... mParameters
    ) throws NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, SQLException, NamingException, InvalidKeyException {
        return getEncryptedObjectFromQuery(null, mQuery, mType, mParameters);
    }
}