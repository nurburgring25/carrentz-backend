package dev.burikk.carrentz.engine.datasource;

import dev.burikk.carrentz.engine.datasource.annotation.*;
import dev.burikk.carrentz.engine.datasource.exception.NoSuchMarkColumnException;
import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.util.DataTypes;
import dev.burikk.carrentz.engine.util.Models;
import dev.burikk.carrentz.engine.util.Parameters;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Muhammad Irfan
 * @since 23/08/2017 8:45
 */
@SuppressWarnings("WeakerAccess")
public class Column extends BaseColumn {
    //<editor-fold desc="Property">
    protected final int mMaxLength;
    protected final boolean mNotNull;
    protected final String mDefaultValue;
    protected final String mColumnReference;
    protected final String mTableReference;
    protected final boolean mPrimaryKey;
    protected final String mPrimaryKeyConstraintName;
    protected final boolean mUniqueKey;
    protected final List<String> mUniqueKeyConstraintNames;
    protected final List<Matcher> mMatchers;
    //</editor-fold>

    {
        this.mUniqueKeyConstraintNames = new ArrayList<>();
        this.mMatchers = new ArrayList<>();
    }

    protected Column(
            @NotNull String mName,
            @NotNull JDBCType mJDBCType,
            @NotNull Field mField,
            int mMaxLength,
            boolean mNotNull,
            String mDefaultValue,
            String mColumnReference,
            String mTableReference,
            boolean mPrimaryKey,
            String mPrimaryKeyConstraintName,
            boolean mUniqueKey,
            List<String> mUniqueKeyConstraintNames,
            List<Matcher> mMatchers
    ) {
        super(mName, mJDBCType, mField);

        Parameters.requireNotNull(mPrimaryKey, mPrimaryKeyConstraintName, "mPrimaryKeyConstraintName");
        Parameters.requireNotNull(mUniqueKey, mUniqueKeyConstraintNames, "mUniqueKeyConstraintNames");

        this.mMaxLength = mMaxLength;
        this.mNotNull = mNotNull;
        this.mDefaultValue = mDefaultValue;
        this.mColumnReference = mColumnReference;
        this.mTableReference = mTableReference;
        this.mPrimaryKey = mPrimaryKey;
        this.mPrimaryKeyConstraintName = mPrimaryKeyConstraintName;
        this.mUniqueKey = mUniqueKey;

        if (mUniqueKeyConstraintNames != null) {
            this.mUniqueKeyConstraintNames.addAll(mUniqueKeyConstraintNames);
        }

        if (mMatchers != null) {
            this.mMatchers.addAll(mMatchers);
        }
    }

    public static Column valueOf(
            @NotNull Class<? extends Entity> mEntityClass,
            @NotNull Field mField
    ) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");
        Parameters.requireNotNull(mField, "mField");

        if (!mField.isAnnotationPresent(MarkColumn.class)) {
            throw new NoSuchMarkColumnException(mEntityClass, mField);
        }

        MarkColumn mMarkColumn = mField.getAnnotation(MarkColumn.class);

        JDBCType mJDBCType;
        if (JDBCType.NULL != mMarkColumn.jdbcType()) {
            mJDBCType = mMarkColumn.jdbcType();
        } else {
            mJDBCType = DataTypes.getJDBCType(mField.getType());
        }

        String mColumnName;
        if (StringUtils.isNotBlank(mMarkColumn.value())) {
            mColumnName = mMarkColumn.value();
        } else {
            mColumnName = mField.getName();
        }

        String mPrimaryKeyConstraintName = null;
        if (mField.isAnnotationPresent(PrimaryKey.class)) {
            if (mEntityClass.isAnnotationPresent(PrimaryKeyConstraint.class)) {
                PrimaryKeyConstraint mPrimaryKeyConstraint = mEntityClass.getAnnotation(PrimaryKeyConstraint.class);

                if (StringUtils.isNotBlank(mPrimaryKeyConstraint.value())) {
                    mPrimaryKeyConstraintName = mPrimaryKeyConstraint.value();
                }
            }

            if (StringUtils.isBlank(mPrimaryKeyConstraintName)) {
                mPrimaryKeyConstraintName = Models.getTableName(mEntityClass) + "_pk";
            }
        }

        final List<String> mUniqueKeyConstraintNames = new ArrayList<>();

        if (mField.isAnnotationPresent(UniqueKey.class)) {
            UniqueKey mUniqueKey = mField.getAnnotation(UniqueKey.class);

            String mUniqueKeyConstraintName = null;
            if (mEntityClass.isAnnotationPresent(UniqueKeyConstraint.class) || mEntityClass.isAnnotationPresent(UniqueKeyConstraints.class)) {
                UniqueKeyConstraint mUniqueKeyConstraint = Models.getUniqueKeyConstraint(mEntityClass, mUniqueKey.value());

                if (StringUtils.isNotBlank(mUniqueKeyConstraint.value())) {
                    mUniqueKeyConstraintName = mUniqueKeyConstraint.value();
                }
            }

            if (StringUtils.isBlank(mUniqueKeyConstraintName)) {
                mUniqueKeyConstraintName = Models.getTableName(mEntityClass) + "_uk";
            }

            mUniqueKeyConstraintNames.add(mUniqueKeyConstraintName);
        } else if (mField.isAnnotationPresent(UniqueKeys.class)) {
            Models.getRepeatableAnnotations(mField, UniqueKey.class)
                    .stream()
                    .sorted(Comparator.comparingInt(UniqueKey::value))
                    .forEach(mUniqueKey -> {
                        String mUniqueKeyConstraintName = null;
                        if (mEntityClass.isAnnotationPresent(UniqueKeyConstraint.class) || mEntityClass.isAnnotationPresent(UniqueKeyConstraints.class)) {
                            UniqueKeyConstraint mUniqueKeyConstraint = Models.getUniqueKeyConstraint(mEntityClass, mUniqueKey.value());

                            if (StringUtils.isNotBlank(mUniqueKeyConstraint.value())) {
                                mUniqueKeyConstraintName = mUniqueKeyConstraint.value();
                            }
                        }

                        if (StringUtils.isBlank(mUniqueKeyConstraintName)) {
                            mUniqueKeyConstraintName = Models.getTableName(mEntityClass) + "_uk";
                        }

                        mUniqueKeyConstraintNames.add(mUniqueKeyConstraintName);
                    });
        }

        final List<Matcher> mMatchers = new ArrayList<>();

        if (mField.isAnnotationPresent(UniqueKeyMatcher.class)) {
            UniqueKeyMatcher mUniqueKeyMatcher = mField.getAnnotation(UniqueKeyMatcher.class);

            if (mEntityClass.isAnnotationPresent(UniqueKeyConstraint.class) || mEntityClass.isAnnotationPresent(UniqueKeyConstraints.class)) {
                UniqueKeyConstraint mUniqueKeyConstraint = Models.getUniqueKeyConstraint(mEntityClass, mUniqueKeyMatcher.id());

                if (StringUtils.isNotBlank(mUniqueKeyConstraint.value())) {
                    mMatchers.add(new Matcher(mUniqueKeyMatcher.id(), mUniqueKeyConstraint.value(), mUniqueKeyMatcher.operator(), Arrays.asList(mUniqueKeyMatcher.value())));
                } else {
                    throw new NullPointerException("Unique key constraint name must be specified.");
                }
            }
        } else if (mField.isAnnotationPresent(UniqueKeyMatchers.class)) {
            Models.getRepeatableAnnotations(mField, UniqueKeyMatcher.class)
                    .stream()
                    .sorted(Comparator.comparingInt(UniqueKeyMatcher::id))
                    .forEach(mUniqueKeyMatcher -> {
                        if (mEntityClass.isAnnotationPresent(UniqueKeyConstraint.class) || mEntityClass.isAnnotationPresent(UniqueKeyConstraints.class)) {
                            UniqueKeyConstraint mUniqueKeyConstraint = Models.getUniqueKeyConstraint(mEntityClass, mUniqueKeyMatcher.id());

                            if (StringUtils.isNotBlank(mUniqueKeyConstraint.value())) {
                                mMatchers.add(new Matcher(mUniqueKeyMatcher.id(), mUniqueKeyConstraint.value(), mUniqueKeyMatcher.operator(), Arrays.asList(mUniqueKeyMatcher.value())));
                            } else {
                                throw new NullPointerException("Unique key constraint name must be specified.");
                            }
                        }
                    });
        }

        String mDefaultValue;
        if (mMarkColumn.defaultValue().contains("*WF*")) {
            String mSequenceName = mMarkColumn.defaultValue().substring(mMarkColumn.defaultValue().indexOf("*WF*") + 4);

            mDefaultValue = "fn_get_sequence_id('" + mSequenceName + "'::character varying)";
        } else if (mMarkColumn.defaultValue().contains("*NV*")) {
            String mSequenceName = mMarkColumn.defaultValue().substring(mMarkColumn.defaultValue().indexOf("*NV*") + 4);

            mDefaultValue = "NEXTVAL('" + mSequenceName + "'::character varying)";
        } else {
            mDefaultValue = mMarkColumn.defaultValue();
        }

        boolean mPrimaryKey = StringUtils.isNotBlank(mPrimaryKeyConstraintName);
        boolean mUniqueKey = !mUniqueKeyConstraintNames.isEmpty();

        return new Column(mColumnName, mJDBCType, mField, mMarkColumn.maxLength(), mMarkColumn.isNotNull(), mDefaultValue, mMarkColumn.columnReference(), mMarkColumn.tableReference(), mPrimaryKey, mPrimaryKeyConstraintName, mUniqueKey, mUniqueKeyConstraintNames, mMatchers);
    }

    //<editor-fold desc="Getter">
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

    public List<String> getUniqueKeyConstraintNames() {
        return this.mUniqueKeyConstraintNames;
    }

    public List<Matcher> getMatchers() {
        return this.mMatchers;
    }
    //</editor-fold>
}