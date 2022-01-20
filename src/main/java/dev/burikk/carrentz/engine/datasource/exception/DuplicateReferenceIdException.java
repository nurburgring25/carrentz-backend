package dev.burikk.carrentz.engine.datasource.exception;

import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.util.Parameters;
import javax.validation.constraints.NotNull;

/**
 * @author Muhammad Irfan
 * @since 20/06/2017 11:21
 */
public class DuplicateReferenceIdException extends RuntimeException {
    private final Class<? extends Entity> mEntityClass;
    private final Integer mReferenceID;

    public DuplicateReferenceIdException(
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
        return "Duplicate @Reference id " + this.mReferenceID + " at " + this.mEntityClass.getSimpleName() + " class.";
    }
}