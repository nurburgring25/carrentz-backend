package dev.burikk.carrentz.engine.entity;

import dev.burikk.carrentz.engine.common.*;
import dev.burikk.carrentz.engine.datasource.Column;
import dev.burikk.carrentz.engine.datasource.DMLManager;
import dev.burikk.carrentz.engine.datasource.annotation.MarkColumn;
import dev.burikk.carrentz.engine.datasource.annotation.Reference;
import dev.burikk.carrentz.engine.entity.annotation.*;
import dev.burikk.carrentz.engine.exception.WynixException;
import dev.burikk.carrentz.engine.util.Models;
import dev.burikk.carrentz.engine.util.Parameters;
import dev.burikk.carrentz.engine.util.Validators;
import dev.burikk.carrentz.engine.validator.annotation.Email;
import dev.burikk.carrentz.engine.validator.annotation.Phone;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import javax.naming.NamingException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.burikk.carrentz.engine.common.Constant.Reflection.*;

/**
 * @author Muhammad Irfan
 * @since 8/22/2017 8:38 PM
 */
public class Entity implements WynixResult, Serializable, Cloneable {
    private static final long serialVersionUID = -8962636722654514742L;

    //<editor-fold desc="Column">
    @MarkColumn(
            value = "created",
            jdbcType = JDBCType.TIMESTAMP,
            isNotNull = true,
            defaultValue = "NOW()"
    )
    @MarkDescription(value = "Created", visible = false)
    private LocalDateTime created;

    @MarkColumn(
            value = "creator",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 32,
            isNotNull = true
    )
    @MarkDescription(value = "Creator", visible = false)
    private String creator;

    @MarkColumn(
            value = "modified",
            jdbcType = JDBCType.TIMESTAMP,
            defaultValue = "NOW()"
    )
    @MarkDescription(value = "Modified", visible = false)
    private LocalDateTime modified;

    @MarkColumn(
            value = "modificator",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 32
    )
    @MarkDescription(value = "Modificator", visible = false)
    private String modificator;

    @MarkColumn(
            value = "deleted",
            jdbcType = JDBCType.BIT,
            isNotNull = true,
            defaultValue = "FALSE"
    )
    @MarkDescription(value = "Deleted", used = false)
    private boolean deleted;
    //</editor-fold>

    //<editor-fold desc="Property">
    private int intFlag;
    private Entity mOldEntity;
    //</editor-fold>

    //<editor-fold desc="Marking">
    public void markDelete() {
        this.mOldEntity = SerializationUtils.clone(this);
        this.intFlag = (this.intFlag & ~7) | 4;
    }

    public void markUpdate() {
        this.mOldEntity = SerializationUtils.clone(this);
        this.intFlag = (this.intFlag & ~7) | 2;
    }

    public void markNew() {
        this.intFlag = (this.intFlag & ~7) | 1;
    }

    public void markUnmodified() {
        this.intFlag = (this.intFlag & ~7);
    }
    //</editor-fold>

    public void doNew() throws SQLException, InstantiationException, IllegalAccessException, NamingException {
        EntityDesign mEntityDesign = EntityCache.getInstance().getEntityDesign(this.getClass());

        mEntityDesign.getColumnMap().entrySet()
                .stream()
                .filter(mEntry -> {
                    if (this.isAuditable()) {
                        switch (mEntry.getKey().getName()) {
                            case FIELD_CREATED:
                                return false;
                            case FIELD_CREATOR:
                                return false;
                        }
                    }

                    return true;
                })
                .forEach(mEntry -> {
                    Field mField = mEntry.getKey();
                    Column mColumn = mEntry.getValue();

                    Description mDescription = mEntityDesign.getDescription(mField.getName());

                    String mFieldDescription;

                    if (mDescription != null) {
                        mFieldDescription = LanguageManager.retrieve(SessionManager.getInstance().getLocale(), mDescription.getKey());
                    } else {
                        mFieldDescription = mField.getName();
                    }

                    try {
                        if (!mField.isAccessible()) {
                            mField.setAccessible(true);
                        }

                        Object mObject = mField.get(this);

                        if (!mColumn.isPrimaryKey()) {
                            if (mColumn.isNotNull()) {
                                IgnoreNotNull ignoreNotNull = mField.getAnnotation(IgnoreNotNull.class);

                                if (ignoreNotNull == null || !ignoreNotNull.onInsert()) {
                                    if (Objects.equals(String.class, mField.getType())) {
                                        if (StringUtils.isBlank(mColumn.getDefaultValue())) {
                                            Validators.mandatory(mObject, mFieldDescription);
                                        }
                                    } else {
                                        if (mObject == null && StringUtils.isBlank(mColumn.getDefaultValue())) {
                                            throw new WynixException(mFieldDescription + " is mandatory.");
                                        }
                                    }
                                }
                            }
                        }

                        if (mColumn.getJDBCType() != JDBCType.LONGVARCHAR) {
                            if (Objects.equals(String.class, mField.getType())) {
                                if (mObject != null) {
                                    if (((String) mObject).length() > mColumn.getMaxLength()) {
                                        throw new WynixException(mFieldDescription + " max characters is " + mColumn.getMaxLength() + ".");
                                    }

                                    if (!mField.isAnnotationPresent(AllowSpecialCharacter.class) && !mField.isAnnotationPresent(Phone.class) && !this.getClass().isAnnotationPresent(AllowSpecialCharacter.class)) {
                                        // TODO: Bypassed
                                        // Validators.detectSpecialCharacter((String) mObject, mFieldDescription);
                                    }
                                }
                            }
                        }

                        if (mField.isAnnotationPresent(Email.class)) {
                            if (Objects.equals(String.class, mField.getType())) {
                                Validators.isEmailValid((String) mObject, mFieldDescription);
                            }
                        }

                        if (mField.isAnnotationPresent(Phone.class)) {
                            if (Objects.equals(String.class, mField.getType())) {
                                Validators.isPhoneValid((String) mObject, mFieldDescription);
                            }
                        }
                    } catch (IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                });

        if (this.isAuditable()) {
            WynixUser mWynixUser = SessionManager.getInstance().getWynixUser();

            Parameters.requireNotNull(mWynixUser, "mWynixUser");

            this.setCreator(mWynixUser.getIdentity());

            if (Constant.Application.USING_SERVER_TIME) {
                this.setCreated(TimeManager.getInstance().now());
            }
        }
    }

    public void doUpdate() throws SQLException, InstantiationException, IllegalAccessException, NamingException {
        EntityDesign mEntityDesign = EntityCache.getInstance().getEntityDesign(this.getClass());

        mEntityDesign.getColumnMap().entrySet()
                .stream()
                .filter(mEntry -> {
                    if (this.isAuditable()) {
                        switch (mEntry.getKey().getName()) {
                            case FIELD_MODIFIED:
                                return false;
                            case FIELD_MODIFICATOR:
                                return false;
                        }
                    }

                    return true;
                })
                .forEach(mEntry -> {
                    Field mField = mEntry.getKey();
                    Column mColumn = mEntry.getValue();

                    Description mDescription = mEntityDesign.getDescription(mField.getName());

                    String mFieldDescription;

                    if (mDescription != null) {
                        mFieldDescription = LanguageManager.retrieve(SessionManager.getInstance().getLocale(), mDescription.getKey());
                    } else {
                        mFieldDescription = mField.getName();
                    }

                    try {
                        if (!mField.isAccessible()) {
                            mField.setAccessible(true);
                        }

                        Object mObject = mField.get(this);

                        if (!mColumn.isPrimaryKey()) {
                            if (mColumn.isNotNull()) {
                                IgnoreNotNull ignoreNotNull = mField.getAnnotation(IgnoreNotNull.class);

                                if (ignoreNotNull == null || !ignoreNotNull.onUpdate()) {
                                    if (Objects.equals(String.class, mField.getType())) {
                                        if (StringUtils.isBlank(mColumn.getDefaultValue())) {
                                            Validators.mandatory(mObject, mFieldDescription);
                                        }
                                    } else {
                                        if (mObject == null && StringUtils.isBlank(mColumn.getDefaultValue())) {
                                            throw new WynixException(mFieldDescription + " is mandatory.");
                                        }
                                    }
                                }
                            }
                        }

                        if (mColumn.getJDBCType() != JDBCType.LONGVARCHAR) {
                            if (Objects.equals(String.class, mField.getType())) {
                                if (mObject != null) {
                                    if (((String) mObject).length() > mColumn.getMaxLength()) {
                                        throw new WynixException(mFieldDescription + " max characters is " + mColumn.getMaxLength() + ".");
                                    }

                                    if (!mField.isAnnotationPresent(AllowSpecialCharacter.class) && !mField.isAnnotationPresent(Phone.class) && !this.getClass().isAnnotationPresent(AllowSpecialCharacter.class)) {
                                        // TODO: Bypassed
                                        // Validators.detectSpecialCharacter((String) mObject, mFieldDescription);
                                    }
                                }
                            }
                        }

                        if (mField.isAnnotationPresent(Email.class)) {
                            if (Objects.equals(String.class, mField.getType())) {
                                Validators.isEmailValid((String) mObject, mFieldDescription);
                            }
                        }

                        if (mField.isAnnotationPresent(Phone.class)) {
                            if (Objects.equals(String.class, mField.getType())) {
                                Validators.isPhoneValid((String) mObject, mFieldDescription);
                            }
                        }
                    } catch (IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                });

        if (this.isAuditable()) {
            WynixUser mWynixUser = SessionManager.getInstance().getWynixUser();

            Parameters.requireNotNull(mWynixUser, "mWynixUser");

            this.setModificator(mWynixUser.getIdentity());

            if (Constant.Application.USING_SERVER_TIME) {
                this.setModified(TimeManager.getInstance().now());
            }
        }
    }

    public void doDelete() {
        EntityDesign entityDesign = EntityCache.getInstance().getEntityDesign(this.getClass());

        for (String dependentTableName : entityDesign.getDependentTables()) {
            Class<? extends Entity> dependentClass = Models.getEntityClass(dependentTableName);

            EntityDesign dependentEntityDesign = EntityCache.getInstance().getEntityDesign(dependentClass);

            Reference reference = Models.getReferences(dependentClass)
                    .stream()
                    .filter(value -> StringUtils.equals(value.targetTable(), entityDesign.getTableName()))
                    .findFirst()
                    .orElse(null);

            if (reference != null) {
                if (reference.checkOnDelete()) {
                    StringBuilder stringBuilder = new StringBuilder()
                            .append("SELECT COUNT(0) FROM ")
                            .append(dependentTableName)
                            .append(" WHERE ")
                            .append(reference.sourceColumn())
                            .append(" = ?");

                    if (dependentEntityDesign.getDeletable()) {
                        stringBuilder
                                .append(" AND ")
                                .append(Constant.Reflection.FIELD_DELETED)
                                .append(" = FALSE;");
                    }

                    List<Object> parameters = new ArrayList<>();

                    try {
                        parameters.add(entityDesign.getPrimaryKeyField().get(this));

                        Long count = DMLManager.getObjectFromQuery(stringBuilder.toString(), parameters.toArray());

                        Validators.validate(!Objects.equals(count, 0L), "Data cannot be deleted, because there is data that still depend on it.");
                    } catch (IllegalAccessException | SQLException | NamingException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }

    //<editor-fold desc="Determine status">
    public boolean isDelete() {
        return (this.intFlag & 7) == 4;
    }

    public boolean isUpdate() {
        return (this.intFlag & 7) == 2;
    }

    public boolean isNew() {
        return (this.intFlag & 7) == 1;
    }

    public boolean isUnmodified() {
        return (this.intFlag & 7) == 0;
    }

    public boolean isModified() {
        return (this.intFlag & 7) != 0;
    }

    public boolean isAuditable() {
        return this.getClass().isAnnotationPresent(MarkAuditable.class);
    }

    public boolean isDeletable() {
        return this.getClass().isAnnotationPresent(MarkDeletable.class);
    }

    public Entity old() {
        return this.mOldEntity;
    }
    //</editor-fold>

    //<editor-fold desc="Getter and setter">
    public LocalDateTime getCreated() {
        return this.created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public String getCreator() {
        return this.creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public LocalDateTime getModified() {
        return this.modified;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }

    public String getModificator() {
        return this.modificator;
    }

    public void setModificator(String modificator) {
        this.modificator = modificator;
    }

    public boolean getDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    //</editor-fold>
}