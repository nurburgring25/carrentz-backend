package dev.burikk.carrentz.engine.util;

import dev.burikk.carrentz.engine.common.Constant;
import dev.burikk.carrentz.engine.datasource.*;
import dev.burikk.carrentz.engine.datasource.annotation.MarkTable;
import dev.burikk.carrentz.engine.datasource.annotation.Reference;
import dev.burikk.carrentz.engine.datasource.annotation.UniqueKeyConstraint;
import dev.burikk.carrentz.engine.datasource.enumeration.Operator;
import dev.burikk.carrentz.engine.datasource.exception.DuplicateReferenceIdException;
import dev.burikk.carrentz.engine.datasource.exception.NoSuchMarkTableException;
import dev.burikk.carrentz.engine.datasource.exception.NoSuchReferenceException;
import dev.burikk.carrentz.engine.datasource.exception.NoSuchUniqueKeyConstraintException;
import dev.burikk.carrentz.engine.datasource.meta.ColumnMeta;
import dev.burikk.carrentz.engine.datasource.meta.ConstraintMeta;
import dev.burikk.carrentz.engine.datasource.meta.DatabaseMeta;
import dev.burikk.carrentz.engine.datasource.meta.TableMeta;
import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.entity.annotation.MarkAuditable;
import dev.burikk.carrentz.engine.entity.annotation.MarkDeletable;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static dev.burikk.carrentz.engine.common.Constant.Reflection.*;

/**
 * @author Muhammad Irfan
 * @since 23/08/2017 9:02
 */
public class Models {
    private static final transient Logger LOGGER = LogManager.getLogger(Models.class);

    @SafeVarargs
    public static List<Field> getAnnotatedFields(
            @NotNull Class<? extends Entity> mEntityClass,
            @NotNull Class<? extends Annotation>... mAnnotationClasses
    ) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");
        Parameters.requireNotNull(mAnnotationClasses, "mAnnotationClasses");

        List<Field> mFields = new ArrayList<>();

        Collections.addAll(mFields, mEntityClass.getDeclaredFields());
        Collections.addAll(mFields, mEntityClass.getSuperclass().getDeclaredFields());

        if (mEntityClass.getSuperclass().getSuperclass() != null) {
            Collections.addAll(mFields, mEntityClass.getSuperclass().getSuperclass().getDeclaredFields());
        }

        return mFields
                .stream()
                .filter(mField -> {
                    for (Class<? extends Annotation> mAnnotationClass : mAnnotationClasses) {
                        if (mField.isAnnotationPresent(mAnnotationClass)) {
                            return true;
                        }
                    }

                    return false;
                })
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<Reference> getReferences(@NotNull Class<? extends Entity> mEntityClass) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");

        List<Reference> mReferences = new ArrayList<>();

        Collections.addAll(mReferences, mEntityClass.getAnnotationsByType(Reference.class));
        Collections.addAll(mReferences, mEntityClass.getSuperclass().getAnnotationsByType(Reference.class));

        if (mEntityClass.getSuperclass().getSuperclass() != null) {
            Collections.addAll(mReferences, mEntityClass.getSuperclass().getSuperclass().getAnnotationsByType(Reference.class));
        }

        return mReferences
                .stream()
                .peek(mReference -> mReferences
                        .forEach(mCheckReference -> {
                            if (mCheckReference != mReference) {
                                if (mCheckReference.id() == mReference.id()) {
                                    throw new DuplicateReferenceIdException(mEntityClass, mCheckReference.id());
                                }
                            }
                        }))
                .collect(Collectors.toList());
    }

    public static Reference getReference(
            @NotNull List<Reference> mReferences,
            int mReferenceID
    ) {
        Parameters.requireNotNull(mReferences, "mReferences");
        Parameters.requireLargerThanTo(mReferenceID, 0, "mReferenceID");

        return mReferences
                .stream()
                .filter(mReference -> mReferenceID == mReference.id())
                .findFirst()
                .orElseThrow(() -> new NoSuchReferenceException("@Reference annotation with value " + mReferenceID + " cannot be found at given mReferences."));
    }

    public static Reference getReference(
            @NotNull Class<? extends Entity> mEntityClass,
            int mReferenceID
    ) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");
        Parameters.requireLargerThanTo(mReferenceID, 0, "mReferenceID");

        return getReferences(mEntityClass)
                .stream()
                .filter(mReference -> mReferenceID == mReference.id())
                .findFirst()
                .orElseThrow(() -> new NoSuchReferenceException(mEntityClass, mReferenceID));
    }

    public static <T extends Annotation> List<T> getRepeatableAnnotations(
            @NotNull Class<? extends Entity> mEntityClass,
            @NotNull Class<T> mAnnotationClass
    ) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");
        Parameters.requireNotNull(mAnnotationClass, "mAnnotationClass");

        List<T> mResult = new ArrayList<>();

        Collections.addAll(mResult, mEntityClass.getAnnotationsByType(mAnnotationClass));
        Collections.addAll(mResult, mEntityClass.getSuperclass().getAnnotationsByType(mAnnotationClass));

        if (mEntityClass.getSuperclass().getSuperclass() != null) {
            Collections.addAll(mResult, mEntityClass.getSuperclass().getSuperclass().getAnnotationsByType(mAnnotationClass));
        }

        return mResult;
    }

    public static <T extends Annotation> List<T> getRepeatableAnnotations(
            @NotNull Field mField,
            @NotNull Class<T> mAnnotationClass
    ) {
        Parameters.requireNotNull(mField, "mField");
        Parameters.requireNotNull(mAnnotationClass, "mAnnotationClass");

        List<T> mResult = new ArrayList<>();

        Collections.addAll(mResult, mField.getAnnotationsByType(mAnnotationClass));

        return mResult;
    }

    public static String getTableName(@NotNull Class<? extends Entity> mEntityClass) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");

        if (!mEntityClass.isAnnotationPresent(MarkTable.class)) {
            throw new NoSuchMarkTableException(mEntityClass);
        }

        MarkTable mMarkTable = mEntityClass.getAnnotation(MarkTable.class);

        if (StringUtils.isNotBlank(mMarkTable.value())) {
            return mMarkTable.value();
        } else {
            return mEntityClass.getSimpleName();
        }
    }

    public static Class<? extends Entity> getEntityClass(@NotNull String mTableName) {
        Parameters.requireNotNull(mTableName, "mTableName");

        return Constant.ENTITY_CLASSES
                .stream()
                .filter(mEntityClass -> {
                    if (mEntityClass.isAnnotationPresent(MarkTable.class)) {
                        MarkTable mMarkTable = mEntityClass.getAnnotation(MarkTable.class);

                        return Objects.equals(mTableName, mMarkTable.value());
                    }

                    return false;
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Class with table name " + mTableName + " cannot be found."));
    }

    public static UniqueKeyConstraint getUniqueKeyConstraint(
            @NotNull List<UniqueKeyConstraint> mUniqueKeyConstraints,
            int mUniqueKeyConstraintID
    ) {
        Parameters.requireNotNull(mUniqueKeyConstraints, "mUniqueKeyConstraints");
        Parameters.requireLargerThanTo(mUniqueKeyConstraintID, 0, "mUniqueKeyConstraintID");

        return mUniqueKeyConstraints
                .stream()
                .filter(mUniqueKeyConstraint -> mUniqueKeyConstraintID == mUniqueKeyConstraint.id())
                .findFirst()
                .orElseThrow(() -> new NoSuchUniqueKeyConstraintException("@UniqueKeyConstraint annotation with value " + mUniqueKeyConstraintID + " cannot be found at given mUniqueKeyConstraints."));
    }

    public static UniqueKeyConstraint getUniqueKeyConstraint(
            @NotNull Class<? extends Entity> mEntityClass,
            int mUniqueKeyConstraintID
    ) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");
        Parameters.requireLargerThanTo(mUniqueKeyConstraintID, 0, "mUniqueKeyConstraintID");

        return getRepeatableAnnotations(mEntityClass, UniqueKeyConstraint.class)
                .stream()
                .filter(mUniqueKeyConstraint -> mUniqueKeyConstraintID == mUniqueKeyConstraint.id())
                .findFirst()
                .orElseThrow(() -> new NoSuchUniqueKeyConstraintException(mEntityClass, mUniqueKeyConstraintID));
    }

    public static UniqueKeyConstraint getUniqueKeyConstraint(
            @NotNull Class<? extends Entity> mEntityClass,
            @NotNull String mUniqueKeyConstraintName
    ) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");
        Parameters.requireNotNull(mUniqueKeyConstraintName, "mUniqueKeyConstraintName");

        return getRepeatableAnnotations(mEntityClass, UniqueKeyConstraint.class)
                .stream()
                .filter(mUniqueKeyConstraint -> Objects.equals(mUniqueKeyConstraintName, mUniqueKeyConstraint.value()))
                .findFirst()
                .orElseThrow(() -> new NoSuchUniqueKeyConstraintException(mEntityClass, mUniqueKeyConstraintName));
    }

    public static void scan(
            @NotNull DDLManager mDDLManager,
            @NotNull List<Class<? extends Entity>> mClassEntities,
            @NotNull StringBuilder mStringBuilder
    ) {
        Parameters.requireNotNull(mDDLManager, "mDDLManager");
        Parameters.requireNotNull(mClassEntities, "mClassEntities");
        Parameters.requireNotNull(mStringBuilder, "mStringBuilder");

        mClassEntities
                .forEach(mEntityClass -> {
                    try {
                        Table mTable = Table.valueOf(mEntityClass);

                        if (!DatabaseMeta.isTableExist(mTable.getName())) {
                            mDDLManager.createTable(mTable.getName());

                            mTable.getColumns()
                                    .stream()
                                    .filter(mColumn -> {
                                        if (!mEntityClass.isAnnotationPresent(MarkDeletable.class)) {
                                            if (Objects.equals(FIELD_DELETED, mColumn.getField().getName())) {
                                                return false;
                                            }
                                        }

                                        if (!mEntityClass.isAnnotationPresent(MarkAuditable.class)) {
                                            switch (mColumn.getField().getName()) {
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
                                    .forEach(mColumn -> {
                                        try {
                                            mDDLManager.addColumn(mTable.getName(), mColumn);
                                        } catch (Exception ex) {
                                            throw new RuntimeException(ex);
                                        }
                                    });

                            if (mTable.getPrimaryKeyConstraint() != null) {
                                mDDLManager.setPrimaryKey(mTable.getName(), mTable.getPrimaryKeyConstraint());
                            }

                            if (!mTable.getUniqueKeyConstraints().isEmpty()) {
                                mTable.getUniqueKeyConstraints()
                                        .forEach(mUniqueKeyConstraint -> {
                                            try {
                                                UniqueKeyConstraint mUniqueKeyConstraintAnnotation = Models.getUniqueKeyConstraint(mEntityClass, mUniqueKeyConstraint.getName());

                                                if (mEntityClass.isAnnotationPresent(MarkDeletable.class)) {
                                                    if (mUniqueKeyConstraintAnnotation.includeDeleted()) {
                                                        Matcher mDeletedMatcher = new Matcher(mUniqueKeyConstraintAnnotation.id(), mUniqueKeyConstraintAnnotation.value(), Operator.EQUAL, Collections.singletonList("FALSE"));

                                                        mUniqueKeyConstraint.getMatcherMap().put(
                                                                mTable.getColumns().stream()
                                                                        .filter(mColumn -> Objects.equals(mColumn.getField().getName(), Constant.Reflection.FIELD_DELETED))
                                                                        .findFirst()
                                                                        .orElseThrow(() -> new RuntimeException("No Such field with name " + Constant.Reflection.FIELD_DELETED + ".")),
                                                                Collections.singletonList(mDeletedMatcher)
                                                        );
                                                    }
                                                }

                                                mDDLManager.setUniqueKey(mTable.getName(), mUniqueKeyConstraint);
                                            } catch (Exception ex) {
                                                throw new RuntimeException(ex);
                                            }
                                        });
                            }
                        } else {
                            TableMeta mTableMeta = DatabaseMeta.getTableMeta(mTable.getName());

                            String mTableName = mTableMeta.getTableName();

                            if (mTableMeta.getPrimaryKeyConstraintMeta() == null && mTable.getPrimaryKeyConstraint() != null) {
                                LOGGER.warn("Primary key constraint " + mTable.getPrimaryKeyConstraint().getName() + " on table " + mTableName + " is not exists, but on entity class " + mEntityClass.getName() + " is exists. Try to create primary key.");

                                try {
                                    mDDLManager.setPrimaryKey(mTableName, mTable.getPrimaryKeyConstraint());

                                    LOGGER.info("Primary key constraint is successfully created.");
                                } catch (Exception ex) {
                                    LOGGER.error("Fail to create primary key constraint.");
                                    throw ex;
                                }
                            } else if (mTableMeta.getPrimaryKeyConstraintMeta() != null && mTable.getPrimaryKeyConstraint() == null) {
                                LOGGER.warn("Primary key constraint " + mTableMeta.getPrimaryKeyConstraintMeta().getName() + " on table " + mTableName + " is exists, but on entity class " + mEntityClass.getName() + " isn't. Please fix the difference.");
                                mStringBuilder
                                        .append(mDDLManager.dropConstraintScript(mTableName, mTableMeta.getPrimaryKeyConstraintMeta().getName()))
                                        .append("\n");
                            } else if (mTableMeta.getPrimaryKeyConstraintMeta() != null) {
                                boolean mProblem = false;

                                for (String mColumnName : mTableMeta.getPrimaryKeyConstraintMeta().getColumnNames()) {
                                    boolean mFound = false;

                                    for (String mColumnNameCheck : mTable.getPrimaryKeyConstraint().getColumnNames()) {
                                        if (mColumnName.equals(mColumnNameCheck)) {
                                            mFound = true;
                                        }
                                    }

                                    if (!mFound) {
                                        mProblem = true;
                                        LOGGER.warn("Column " + mColumnName + " is registered on primary key " + mTableMeta.getPrimaryKeyConstraintMeta().getName() + ", but on entity class " + mEntityClass.getName() + " isn't. Please fix the difference.");
                                    }
                                }

                                for (String mColumnName : mTable.getPrimaryKeyConstraint().getColumnNames()) {
                                    boolean mFound = false;

                                    for (String mColumnNameCheck : mTableMeta.getPrimaryKeyConstraintMeta().getColumnNames()) {
                                        if (mColumnName.equals(mColumnNameCheck)) {
                                            mFound = true;
                                        }
                                    }

                                    if (!mFound) {
                                        mProblem = true;
                                        LOGGER.warn("Column " + mColumnName + " isn't registered on primary key " + mTableMeta.getPrimaryKeyConstraintMeta().getName() + ", but on entity class " + mEntityClass.getName() + " is registered. Please fix the difference.");
                                    }
                                }

                                if (mProblem) {
                                    mStringBuilder
                                            .append(mDDLManager.dropConstraintScript(mTableName, mTableMeta.getPrimaryKeyConstraintMeta().getName()))
                                            .append("\n")
                                            .append(mDDLManager.setPrimaryKeyScript(mTableName, mTable.getPrimaryKeyConstraint()))
                                            .append("\n");
                                }
                            }

                            if (mTableMeta.getUniqueKeyConstraintMetas().isEmpty() && !mTable.getUniqueKeyConstraints().isEmpty()) {
                                LOGGER.warn("There is no unique keys on table " + mTableName + ", but on entity class " + mEntityClass.getName() + " is available. Try to create unique keys.");

                                try {
                                    for (Constraint mUKConstraint : mTable.getUniqueKeyConstraints()) {
                                        UniqueKeyConstraint mUniqueKeyConstraintAnnotation = Models.getUniqueKeyConstraint(mEntityClass, mUKConstraint.getName());

                                        if (mEntityClass.isAnnotationPresent(MarkDeletable.class)) {
                                            if (mUniqueKeyConstraintAnnotation.includeDeleted()) {
                                                Matcher mDeletedMatcher = new Matcher(mUniqueKeyConstraintAnnotation.id(), mUniqueKeyConstraintAnnotation.value(), Operator.EQUAL, Collections.singletonList("FALSE"));

                                                mUKConstraint.getMatcherMap().put(
                                                        mTable.getColumns().stream()
                                                                .filter(mColumn -> Objects.equals(mColumn.getField().getName(), Constant.Reflection.FIELD_DELETED))
                                                                .findFirst()
                                                                .orElseThrow(() -> new RuntimeException("No Such field with name " + Constant.Reflection.FIELD_DELETED + ".")),
                                                        Collections.singletonList(mDeletedMatcher)
                                                );
                                            }
                                        }

                                        mDDLManager.setUniqueKey(mTableName, mUKConstraint);
                                    }

                                    LOGGER.info("Unique keys is successfully created.");
                                } catch (Exception ex) {
                                    LOGGER.error("Fail to create unique keys.");
                                    throw ex;
                                }
                            } else if (!mTableMeta.getUniqueKeyConstraintMetas().isEmpty() && mTable.getUniqueKeyConstraints().isEmpty()) {
                                LOGGER.warn("There is unique keys on table " + mTableName + ", but on entity class " + mEntityClass.getName() + " isn't. Please fix the differences.");
                                for (ConstraintMeta mUKConstraintMeta : mTableMeta.getUniqueKeyConstraintMetas()) {
                                    mStringBuilder
                                            .append(mDDLManager.dropConstraintScript(mTableName, mUKConstraintMeta.getName()))
                                            .append("\n");
                                }
                            } else if (!mTableMeta.getUniqueKeyConstraintMetas().isEmpty()) {
                                for (ConstraintMeta mUKConstraintMeta : mTableMeta.getUniqueKeyConstraintMetas()) {
                                    boolean mFound = false;

                                    for (Constraint mUKConstraintCheck : mTable.getUniqueKeyConstraints()) {
                                        if (mUKConstraintMeta.getName().equals(mUKConstraintCheck.getName())) {
                                            boolean mProblem = false;

                                            for (String mColumnName : mUKConstraintMeta.getColumnNames()) {
                                                boolean mFoundColumn = false;

                                                for (String mColumnNameCheck : mUKConstraintCheck.getColumnNames()) {
                                                    if (mColumnName.equals(mColumnNameCheck)) {
                                                        mFoundColumn = true;
                                                    }
                                                }

                                                if (!mFoundColumn) {
                                                    mProblem = true;
                                                    LOGGER.warn("Column " + mColumnName + " is registered on unique key " + mUKConstraintMeta.getName() + ", but on entity class " + mEntityClass.getName() + " isn't. Please fix the difference.");
                                                }
                                            }

                                            for (String mColumnName : mUKConstraintCheck.getColumnNames()) {
                                                boolean mFoundColumn = false;

                                                for (String mColumnNameCheck : mUKConstraintMeta.getColumnNames()) {
                                                    if (mColumnName.equals(mColumnNameCheck)) {
                                                        mFoundColumn = true;
                                                    }
                                                }

                                                if (!mFoundColumn) {
                                                    mProblem = true;
                                                    LOGGER.warn("Column " + mColumnName + " isn't registered on unique key " + mUKConstraintCheck.getName() + ", but on entity class " + mEntityClass.getName() + " is registered. Please fix the difference.");
                                                }
                                            }

                                            if (mProblem) {
                                                mStringBuilder
                                                        .append(mDDLManager.dropConstraintScript(mTableName, mUKConstraintMeta.getName()))
                                                        .append("\n")
                                                        .append(mDDLManager.setUniqueKeyScript(mTableName, mUKConstraintCheck))
                                                        .append("\n");
                                            }

                                            mFound = true;
                                        }
                                    }

                                    if (!mFound) {
                                        LOGGER.warn("Unique key constraint " + mUKConstraintMeta.getName() + " on table " + mTableName + " is exists, but on entity class " + mEntityClass.getName() + " isn't. Please fix the difference.");
                                        mStringBuilder
                                                .append(mDDLManager.dropConstraintScript(mTableName, mUKConstraintMeta.getName()))
                                                .append("\n");
                                    }
                                }

                                for (Constraint mUKConstraint : mTable.getUniqueKeyConstraints()) {
                                    boolean mFound = false;

                                    for (ConstraintMeta mUKConstraintMetaCheck : mTableMeta.getUniqueKeyConstraintMetas()) {
                                        if (mUKConstraint.getName().equals(mUKConstraintMetaCheck.getName())) {
                                            mFound = true;
                                        }
                                    }

                                    if (!mFound) {
                                        LOGGER.warn("Unique key constraint " + mUKConstraint.getName() + " on table " + mTableName + " is not exists, but on entity class " + mEntityClass.getName() + " is exists. Try to create unique key.");

                                        try {
                                            UniqueKeyConstraint mUniqueKeyConstraintAnnotation = Models.getUniqueKeyConstraint(mEntityClass, mUKConstraint.getName());

                                            if (mEntityClass.isAnnotationPresent(MarkDeletable.class)) {
                                                if (mUniqueKeyConstraintAnnotation.includeDeleted()) {
                                                    Matcher mDeletedMatcher = new Matcher(mUniqueKeyConstraintAnnotation.id(), mUniqueKeyConstraintAnnotation.value(), Operator.EQUAL, Collections.singletonList("FALSE"));

                                                    mUKConstraint.getMatcherMap().put(
                                                            mTable.getColumns().stream()
                                                                    .filter(mColumn -> Objects.equals(mColumn.getField().getName(), Constant.Reflection.FIELD_DELETED))
                                                                    .findFirst()
                                                                    .orElseThrow(() -> new RuntimeException("No Such field with name " + Constant.Reflection.FIELD_DELETED + ".")),
                                                            Collections.singletonList(mDeletedMatcher)
                                                    );
                                                }
                                            }

                                            mDDLManager.setUniqueKey(mTableName, mUKConstraint);

                                            LOGGER.info("Unique key constraint is successfully created.");
                                        } catch (Exception ex) {
                                            LOGGER.error("Fail to create unique key constraint.");
                                            throw ex;
                                        }
                                    }
                                }
                            }

                            for (Column mColumn : mTable.getColumns()) {
                                String mColumnName = mColumn.getName();

                                if (!mTableMeta.isColumnExist(mColumnName)) {
                                    // If MarkDeletable isn't present, don't add deleted field.
                                    if (!mEntityClass.isAnnotationPresent(MarkDeletable.class)) {
                                        if (Objects.equals(Constant.Reflection.FIELD_DELETED, mColumn.getField().getName())) {
                                            continue;
                                        }
                                    }

                                    // If MarkAuditable isn't present, don't add created, creator, modified, and modificator field.
                                    if (!mEntityClass.isAnnotationPresent(MarkAuditable.class)) {
                                        switch (mColumn.getField().getName()) {
                                            case Constant.Reflection.FIELD_CREATED:
                                                continue;
                                            case Constant.Reflection.FIELD_CREATOR:
                                                continue;
                                            case Constant.Reflection.FIELD_MODIFIED:
                                                continue;
                                            case Constant.Reflection.FIELD_MODIFICATOR:
                                                continue;
                                        }
                                    }

                                    mDDLManager.addColumn(mTableName, mColumn);
                                } else {
                                    ColumnMeta mColumnMeta = mTableMeta.getColumnMeta(mColumnName);

                                    if (!(mColumn.getJDBCType() == JDBCType.ROWID || mColumn.getJDBCType() == JDBCType.LONGVARCHAR)) {
                                        if (mColumnMeta.getJDBCType() != mColumn.getJDBCType()) {
                                            LOGGER.warn("Data type on " + mColumnName + " column at " + mTableName + " table is difference with entity class " + mEntityClass.getSimpleName() + ".");
                                            mStringBuilder
                                                    .append(mDDLManager.changeColumnTypeScript(mTableName, mColumn))
                                                    .append("\n");
                                        }
                                    }

                                    if (!mColumnMeta.isNotNull() && mColumn.isNotNull()) {
                                        LOGGER.warn("Column " + mColumnName + " on table " + mTableName + " is nullable, but in entity class " + mEntityClass.getSimpleName() + " is not null. Try to changing the attribute...");

                                        try {
                                            mDDLManager.markNotNull(mTableName, mColumnName);
                                        } catch (Exception ex) {
                                            LOGGER.error("Fail changing attribute to not null, please fix the difference.");
                                            throw ex;
                                        }
                                    } else if (mColumnMeta.isNotNull() && !mColumn.isNotNull()) {
                                        LOGGER.warn("Column " + mColumnName + " on table " + mTableName + " is not null, but in entity class " + mEntityClass.getSimpleName() + " is nullable. Try to changing the attribute...");

                                        mDDLManager.markNullable(mTableName, mColumnName);

                                        LOGGER.info("Success changing attribute to nullable.");
                                    }

                                    if (mColumn.getMaxLength() != 0) {
                                        if (mColumn.getMaxLength() != mColumnMeta.getMaxLength()) {
                                            LOGGER.warn("Column size on " + mColumnName + " column at " + mTableName + " table is difference with entity class " + mEntityClass.getSimpleName() + ".");
                                            mStringBuilder
                                                    .append(mDDLManager.changeColumnTypeScript(mTableName, mColumn))
                                                    .append("\n");
                                        }
                                    }

                                    if (StringUtils.isNotBlank(mColumn.getDefaultValue())) {
                                        if (mColumnMeta.getDefaultValue() != null) {
                                            if (!mColumn.getDefaultValue().equalsIgnoreCase(mColumnMeta.getDefaultValue())) {
                                                LOGGER.warn("Default value on " + mColumnName + " column at " + mTableName + " table is difference with entity class " + mEntityClass.getSimpleName() + ".");
                                                mStringBuilder
                                                        .append(mDDLManager.setDefaultValueScript(mTableName, mColumnName, mColumn.getDefaultValue()))
                                                        .append("\n");
                                            }
                                        } else {
                                            mDDLManager.setDefaultValue(mTableName, mColumnName, mColumn.getDefaultValue());

                                            LOGGER.info("Success changing default value.");
                                        }
                                    }

                                    if (StringUtils.isNotBlank(mColumn.getTableReference())) {
                                        if (mColumnMeta.getTableReference() != null) {
                                            if (!mColumn.getTableReference().equalsIgnoreCase(mColumnMeta.getTableReference())) {
                                                LOGGER.warn("Table reference on " + mColumnName + " column at " + mTableName + " table is difference with entity class " + mEntityClass.getSimpleName() + ".");
                                                mStringBuilder
                                                        .append(mDDLManager.dropConstraintScript(mTableName, mTableName + "_" + mColumnMeta.getName() + "_fkey"))
                                                        .append("\n")
                                                        .append(mDDLManager.setTableReferenceScript(mTableName, mColumnName, mColumn.getColumnReference(), mColumn.getTableReference()))
                                                        .append("\n");
                                            }
                                        } else {
                                            mDDLManager.setTableReference(mTableName, mColumnName, mColumn.getColumnReference(), mColumn.getTableReference());

                                            LOGGER.info("Success changing table reference.");
                                        }

                                        if (StringUtils.isNotBlank(mColumn.getColumnReference())) {
                                            if (mColumnMeta.getColumnReference() != null) {
                                                if (!mColumn.getColumnReference().equalsIgnoreCase(mColumnMeta.getColumnReference())) {
                                                    LOGGER.warn("Column reference at " + mTableName + " table is difference with entity class " + mEntityClass.getSimpleName() + ".");
                                                    mStringBuilder
                                                            .append(mDDLManager.dropConstraintScript(mTableName, mTableName + "_" + mColumnMeta.getName() + "_fkey"))
                                                            .append("\n")
                                                            .append(mDDLManager.setTableReferenceScript(mTableName, mColumnName, mColumn.getColumnReference(), mColumn.getTableReference()))
                                                            .append("\n");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        LOGGER.catching(ex);
                    }
                });
    }
}