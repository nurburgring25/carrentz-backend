package dev.burikk.carrentz.engine.datasource.exception;

import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.util.Parameters;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Muhammad Irfan
 * @since 20/06/2017 9:09
 */
public class NoSuchReferenceException extends RuntimeException {
    private Class<? extends Entity> mEntityClass;
    private Integer mReferenceID;

    public NoSuchReferenceException(String message) {
        super(message);
    }

    public NoSuchReferenceException(
            @NotNull Class<? extends Entity> mEntityClass,
            @NotNull Integer mReferenceID
    ) {
        Parameters.requireNotNull(mEntityClass, "mEntityClass");
        Parameters.requireLargerThanTo(mReferenceID, 0, "mReferenceID");

        this.mEntityClass = mEntityClass;
        this.mReferenceID = mReferenceID;
    }

    @Override
    public String getMessage() {
        if (StringUtils.isNotBlank(super.getMessage())) {
            return super.getMessage();
        } else {
            return "@Reference annotation with id " + this.mReferenceID + " cannot be found at " + this.mEntityClass.getSimpleName() + " class.";
        }
    }
}