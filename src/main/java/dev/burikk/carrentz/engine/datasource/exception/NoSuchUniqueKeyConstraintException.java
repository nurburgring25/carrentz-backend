package dev.burikk.carrentz.engine.datasource.exception;

import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.util.Parameters;
import com.sun.istack.internal.NotNull;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Muhammad Irfan
 * @since 20/06/2017 12:10
 */
public class NoSuchUniqueKeyConstraintException extends RuntimeException {
    private Class<? extends Entity> mEntityClass;
    private Integer mUniqueKeyConstraintID;
    private String mUniqueKeyConstraintName;

    public NoSuchUniqueKeyConstraintException(String message) {
        super(message);
    }

    public NoSuchUniqueKeyConstraintException(
            @NotNull Class<? extends Entity> mEntityClass,
            @NotNull Integer mUniqueKeyConstraintID
    ) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");
        Parameters.requireLargerThanTo(mUniqueKeyConstraintID, 0, "mUniqueKeyConstraintID");

        this.mEntityClass = mEntityClass;
        this.mUniqueKeyConstraintID = mUniqueKeyConstraintID;
    }

    public NoSuchUniqueKeyConstraintException(
            @NotNull Class<? extends Entity> mEntityClass,
            @NotNull String mUniqueKeyConstraintName
    ) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");
        Parameters.requireNotNull(mUniqueKeyConstraintName, "mUniqueKeyConstraintName");

        this.mEntityClass = mEntityClass;
        this.mUniqueKeyConstraintName = mUniqueKeyConstraintName;
    }

    @Override
    public String getMessage() {
        if (StringUtils.isNotBlank(super.getMessage())) {
            return super.getMessage();
        } else {
            if (this.mUniqueKeyConstraintID != null) {
                return "@UniqueKeyConstraint annotation with id " + this.mUniqueKeyConstraintID + " cannot be found at " + this.mEntityClass.getSimpleName() + " class.";
            } else {
                return "@UniqueKeyConstraint annotation with name " + this.mUniqueKeyConstraintName + " cannot be found at " + this.mEntityClass.getSimpleName() + " class.";
            }
        }
    }
}