package dev.burikk.carrentz.engine.entity;

import dev.burikk.carrentz.engine.datasource.Column;
import dev.burikk.carrentz.engine.datasource.ReferencedColumn;
import dev.burikk.carrentz.engine.util.Parameters;
import javax.validation.constraints.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Muhammad Irfan
 * @since 23/08/2017 13:26
 */
public class EntityCache {
    private static final EntityCache INSTANCE;

    static {
        INSTANCE = new EntityCache();
    }

    private final Map<Class<? extends Entity>, EntityDesign> mMap;

    {
        this.mMap = new HashMap<>();
    }

    public EntityCache() {}

    public static EntityCache getInstance() {
        return INSTANCE;
    }

    private void scan(@NotNull Class<? extends Entity> mEntityClass) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");

        this.mMap.put(mEntityClass, new EntityDesign(mEntityClass));
    }

    public EntityDesign getEntityDesign(@NotNull Class<? extends Entity> mEntityClass) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");

        if (!this.mMap.containsKey(mEntityClass)) {
            this.scan(mEntityClass);
        }

        return this.mMap.get(mEntityClass);
    }

    public String getTableName(@NotNull Class<? extends Entity> mEntityClass) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");

        return this.getEntityDesign(mEntityClass).getTableName();
    }

    public Column getColumn(
            @NotNull Field mField,
            @NotNull Class<? extends Entity> mEntityClass
    ) {
        Parameters.requireNotNull(mField, "mField");
        Parameters.requireNotNull(mEntityClass, "mEntityClass");

        return (Column) this.getEntityDesign(mEntityClass).getBaseColumn(mField.getName());
    }

    public ReferencedColumn getReferencedColumn(
            @NotNull Field mField,
            @NotNull Class<? extends Entity> mEntityClass
    ) {
        Parameters.requireNotNull(mField, "mField");
        Parameters.requireNotNull(mEntityClass, "mEntityClass");

        return (ReferencedColumn) this.getEntityDesign(mEntityClass).getBaseColumn(mField.getName());
    }
}