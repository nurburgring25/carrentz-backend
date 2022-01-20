package dev.burikk.carrentz.engine.entity;

import dev.burikk.carrentz.engine.common.LanguageManager;
import dev.burikk.carrentz.engine.common.SessionManager;
import dev.burikk.carrentz.engine.datasource.*;
import dev.burikk.carrentz.engine.datasource.annotation.MarkColumn;
import dev.burikk.carrentz.engine.datasource.annotation.MarkEncryptedColumn;
import dev.burikk.carrentz.engine.datasource.annotation.MarkReferencedColumn;
import dev.burikk.carrentz.engine.datasource.annotation.MarkTable;
import dev.burikk.carrentz.engine.datasource.exception.NoSuchMarkTableException;
import dev.burikk.carrentz.engine.entity.annotation.MarkAuditable;
import dev.burikk.carrentz.engine.entity.annotation.MarkDeletable;
import dev.burikk.carrentz.engine.entity.annotation.MarkDescription;
import dev.burikk.carrentz.engine.util.Models;
import dev.burikk.carrentz.engine.util.Parameters;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static dev.burikk.carrentz.engine.common.Constant.Reflection.*;

/**
 * @author Muhammad Irfan
 * @since 23/08/2017 12:21
 */
public class EntityDesign {
    //<editor-fold desc="Property">
    private Boolean mDeletable;

    private Table mTable;
    private String mTableName;
    private final LinkedHashMap<Field, BaseColumn> mMap;
    private Field mPrimaryKeyField;
    private Column mPrimaryKeyColumn;
    private final List<String> mDependOnTables;
    private final List<String> mDependentTables;
    private final List<Description> mDescriptions;
    //</editor-fold>

    {
        this.mMap = new LinkedHashMap<>();
        this.mDependOnTables = new ArrayList<>();
        this.mDependentTables = new ArrayList<>();
        this.mDescriptions = new ArrayList<>();
    }

    EntityDesign(@NotNull Class<? extends Entity> mEntityClass) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");

        if (!mEntityClass.isAnnotationPresent(MarkTable.class)) {
            throw new NoSuchMarkTableException(mEntityClass);
        }

        this.mDeletable = mEntityClass.isAnnotationPresent(MarkDeletable.class);
        this.mTable = Table.valueOf(mEntityClass);

        MarkTable mMarkTable = mEntityClass.getAnnotation(MarkTable.class);

        if (StringUtils.isNotBlank(mMarkTable.value())) {
            this.mTableName = mMarkTable.value();
        } else {
            this.mTableName = mEntityClass.getSimpleName();
        }

        Models.getAnnotatedFields(mEntityClass, MarkColumn.class, MarkEncryptedColumn.class, MarkReferencedColumn.class)
                .stream()
                .filter(mField -> {
                    if (!mEntityClass.isAnnotationPresent(MarkDeletable.class)) {
                        if (Objects.equals(FIELD_DELETED, mField.getName())) {
                            return false;
                        }
                    }

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

                    return true;
                })
                .forEach(mField -> {
                    if (mField.isAnnotationPresent(MarkColumn.class)) {
                        Column mColumn = Column.valueOf(mEntityClass, mField);

                        if (mColumn.isPrimaryKey()) {
                            this.mPrimaryKeyField = mField;
                            this.mPrimaryKeyColumn = mColumn;
                        }

                        if (StringUtils.isNotBlank(mColumn.getTableReference())) {
                            if (!Objects.equals(mColumn.getTableReference(), this.mTableName)) {
                                if (!this.mDependOnTables.contains(mColumn.getTableReference())) {
                                    this.mDependOnTables.add(mColumn.getTableReference());

                                    EntityCache.getInstance().getEntityDesign(Models.getEntityClass(mColumn.getTableReference())).getDependentTables().add(this.mTableName);
                                }
                            }
                        }

                        this.mMap.put(mField, mColumn);
                    } else if (mField.isAnnotationPresent(MarkEncryptedColumn.class)) {
                        EncryptedColumn mEncryptedColumn = EncryptedColumn.valueOf(mEntityClass, mField);

                        this.mMap.put(mField, mEncryptedColumn);
                    } else {
                        ReferencedColumn mReferencedColumn = ReferencedColumn.valueOf(mEntityClass, mField);

                        this.mMap.put(mField, mReferencedColumn);
                    }

                    if (mField.isAnnotationPresent(MarkDescription.class)) {
                        MarkDescription mMarkDescription = mField.getAnnotation(MarkDescription.class);

                        Description mDescription = new Description();

                        mDescription.setFieldName(mField.getName());
                        mDescription.setFieldVisible(mMarkDescription.visible());
                        mDescription.setUsed(mMarkDescription.used());
                        mDescription.setKey(mMarkDescription.value());

                        this.mDescriptions.add(mDescription);
                    }
                });
    }

    BaseColumn getBaseColumn(@NotNull String mFieldName) {
        Parameters.requireNotNull(mFieldName, "mFieldName");

        return this.mMap.get(
                this.mMap.keySet()
                        .stream()
                        .filter(mField -> Objects.equals(mFieldName, mField.getName()))
                        .findFirst()
                        .orElse(null)
        );
    }

    public Description getDescription(@NotNull String mFieldName) {
        Parameters.requireNotNull(mFieldName, "mFieldName");

        return this.mDescriptions
                .stream()
                .filter(mDescription -> Objects.equals(mDescription.getFieldName(), mFieldName))
                .findFirst()
                .orElse(null);
    }

    public String getFieldDescription(@NotNull String mFieldName) {
        Parameters.requireNotNull(mFieldName, "mFieldName");

        Description description = this.getDescription(mFieldName);

        if (description != null) {
            return LanguageManager.retrieve(SessionManager.getInstance().getLocale(), description.getKey());
        }

        return "";
    }

    public Map<Field, Column> getColumnMap() {
        return this.mMap.entrySet()
                .stream()
                .filter(mEntry -> mEntry.getValue() instanceof Column)
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                p -> (Column) p.getValue(),
                                (u, v) -> {
                                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                                },
                                LinkedHashMap::new
                        )
                );
    }

    public Map<Field, ReferencedColumn> getReferencedColumnMap() {
        return this.mMap.entrySet()
                .stream()
                .filter(mEntry -> mEntry.getValue() instanceof ReferencedColumn)
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                p -> (ReferencedColumn) p.getValue(),
                                (u, v) -> {
                                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                                },
                                LinkedHashMap::new
                        )
                );
    }

    //<editor-fold desc="Getter">
    public Boolean getDeletable() {
        return this.mDeletable;
    }

    public Table getTable() {
        return this.mTable;
    }

    public String getTableName() {
        return this.mTableName;
    }

    public Map<Field, BaseColumn> getMap() {
        return this.mMap;
    }

    public Field getPrimaryKeyField() {
        return this.mPrimaryKeyField;
    }

    public Column getPrimaryKeyColumn() {
        return this.mPrimaryKeyColumn;
    }

    public List<String> getDependOnTables() {
        return this.mDependOnTables;
    }

    public List<String> getDependentTables() {
        return this.mDependentTables;
    }

    public List<Description> getDescriptions() {
        return this.mDescriptions;
    }
    //</editor-fold>
}