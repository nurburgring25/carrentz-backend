package dev.burikk.carrentz.engine.datasource;

import dev.burikk.carrentz.engine.datasource.annotation.MarkColumn;
import dev.burikk.carrentz.engine.datasource.annotation.MarkEncryptedColumn;
import dev.burikk.carrentz.engine.datasource.annotation.MarkTable;
import dev.burikk.carrentz.engine.datasource.exception.NoSuchMarkTableException;
import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.entity.annotation.MarkAuditable;
import dev.burikk.carrentz.engine.entity.annotation.MarkDeletable;
import dev.burikk.carrentz.engine.util.Models;
import dev.burikk.carrentz.engine.util.Parameters;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.burikk.carrentz.engine.common.Constant.Reflection.*;

/**
 * @author Muhammad Irfan
 * @since 23/08/2017 11:21
 */
public class Table {
    //<editor-fold desc="Property">
    private String mName;
    private final List<Column> mColumns;
    private Constraint mPrimaryKeyConstraint;
    private final List<Constraint> mUniqueKeyConstraints;
    //</editor-fold>

    {
        this.mColumns = new ArrayList<>();
        this.mUniqueKeyConstraints = new ArrayList<>();
    }

    private Table(
            @NotNull String mName,
            @NotNull List<Column> mColumns,
            @Null Constraint mPrimaryKeyConstraint,
            @NotNull List<Constraint> mUniqueKeyConstraints
    ) {
        Parameters.requireNotNull(mName, "mName");

        this.mName = mName;
        this.mColumns.addAll(mColumns);
        this.mPrimaryKeyConstraint = mPrimaryKeyConstraint;
        this.mUniqueKeyConstraints.addAll(mUniqueKeyConstraints);
    }

    public static Table valueOf(@NotNull Class<? extends Entity> mEntityClass) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");

        if (!mEntityClass.isAnnotationPresent(MarkTable.class)) {
            throw new NoSuchMarkTableException(mEntityClass);
        }

        MarkTable mMarkTable = mEntityClass.getAnnotation(MarkTable.class);

        final List<Column> mColumns = new ArrayList<>();
        final Constraint[] mPrimaryKeyConstraint = {null};
        final List<Constraint> mUniqueKeyConstraints = new ArrayList<>();

        String mName;
        if (StringUtils.isNotBlank(mMarkTable.value())) {
            mName = mMarkTable.value();
        } else {
            mName = mEntityClass.getSimpleName();
        }

        Models.getAnnotatedFields(mEntityClass, MarkColumn.class, MarkEncryptedColumn.class)
                .stream()
                .filter(mField -> {
                    if (!mEntityClass.isAnnotationPresent(MarkAuditable.class)) {
                        switch (mField.getName()) {
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

                    if (!mEntityClass.isAnnotationPresent(MarkDeletable.class)) {
                        return !Objects.equals(FIELD_DELETED, mField.getName());
                    }

                    return true;
                })
                .forEach(mField -> {
                    if (mField.isAnnotationPresent(MarkColumn.class)) {
                        Column mColumn = Column.valueOf(mEntityClass, mField);

                        if (mColumn.isPrimaryKey()) {
                            if (mPrimaryKeyConstraint[0] == null) {
                                mPrimaryKeyConstraint[0] = new Constraint(mColumn.getPrimaryKeyConstraintName());
                                mPrimaryKeyConstraint[0].getMap().put(mField, mColumn);
                            }
                        }

                        if (mColumn.isUniqueKey()) {
                            mColumn.getUniqueKeyConstraintNames().forEach(mUniqueKeyConstraintName -> {
                                boolean[] mFound = {false};

                                mUniqueKeyConstraints
                                        .stream()
                                        .filter(mUniqueKeyConstraint -> Objects.equals(mUniqueKeyConstraint.getName(), mUniqueKeyConstraintName))
                                        .findFirst()
                                        .ifPresent(mUniqueKeyConstraint -> {
                                            mUniqueKeyConstraint.getMap().put(mField, mColumn);
                                            mFound[0] = true;
                                        });

                                if (!mFound[0]) {
                                    Constraint mUniqueKeyConstraint = new Constraint(mUniqueKeyConstraintName);
                                    mUniqueKeyConstraint.getMap().put(mField, mColumn);
                                    mUniqueKeyConstraints.add(mUniqueKeyConstraint);
                                }
                            });
                        }

                        mColumns.add(mColumn);
                    } else {
                        EncryptedColumn mEncryptedColumn = EncryptedColumn.valueOf(mEntityClass, mField);

                        mColumns.add(mEncryptedColumn);
                    }
                });

        mColumns.forEach(mColumn -> mColumn.mMatchers.forEach(mMatcher -> {
            mUniqueKeyConstraints
                    .stream()
                    .filter(mUniqueKeyConstraint -> Objects.equals(mUniqueKeyConstraint.getName(), mMatcher.getName()))
                    .findFirst()
                    .ifPresent(mUniqueKeyConstraint -> {
                        if (mUniqueKeyConstraint.getMatcherMap().containsKey(mColumn)) {
                            List<Matcher> mMatchers = mUniqueKeyConstraint.getMatcherMap().get(mColumn);

                            mMatchers.add(mMatcher);
                        } else {
                            List<Matcher> mMatchers = new ArrayList<>();

                            mMatchers.add(mMatcher);

                            mUniqueKeyConstraint.getMatcherMap().put(mColumn, mMatchers);
                        }
                    });
        }));

        return new Table(mName, mColumns, mPrimaryKeyConstraint[0], mUniqueKeyConstraints);
    }

    //<editor-fold desc="Getter">
    public String getName() {
        return mName;
    }

    public List<Column> getColumns() {
        return mColumns;
    }

    public Constraint getPrimaryKeyConstraint() {
        return mPrimaryKeyConstraint;
    }

    public List<Constraint> getUniqueKeyConstraints() {
        return mUniqueKeyConstraints;
    }
    //</editor-fold>
}