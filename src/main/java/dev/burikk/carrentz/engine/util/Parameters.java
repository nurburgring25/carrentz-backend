package dev.burikk.carrentz.engine.util;

import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Muhammad Irfan
 * @since 8/22/2017 12:14 PM
 */
@SuppressWarnings("WeakerAccess")
public class Parameters {
    public static <T> T requireNotNull(
            boolean mCondition,
            T mObject,
            @NotNull String mParameterName
    ) {
        if (mCondition) {
            requireNotNull(mObject, mParameterName);
        }

        return mObject;
    }

    public static <T> T requireNotNull(
            T mObject,
            @NotNull String mParameterName
    ) {
        if (mParameterName == null) {
            throw new NullPointerException("mParameterName cannot be null.");
        }

        if (mObject == null) {
            throw new IllegalArgumentException(mParameterName + " cannot be null.");
        } else {
            if (mObject instanceof String) {
                if (StringUtils.isEmpty((String) mObject)) {
                    throw new IllegalArgumentException(mParameterName + " cannot be empty.");
                }
            }
        }

        return mObject;
    }

    public static Number requireLargerThanTo(
            @NotNull Integer mInteger,
            @NotNull Integer mOtherInteger,
            @NotNull String mParameterName
    ) {
        requireNotNull(mInteger, mParameterName);
        requireNotNull(mOtherInteger, "mOtherInteger");
        requireNotNull(mParameterName, "mParameterName");

        if(mInteger <= mOtherInteger) {
            throw new IllegalArgumentException(mParameterName + " must be larger than " + mOtherInteger + ".");
        }

        return mInteger;
    }

    public static Number requireLargerThanOrEqualTo(
            @NotNull Integer mInteger,
            @NotNull Integer mOtherInteger,
            @NotNull String mParameterName
    ) {
        requireNotNull(mInteger, mParameterName);
        requireNotNull(mOtherInteger, "mOtherInteger");
        requireNotNull(mParameterName, "mParameterName");

        if(mInteger < mOtherInteger) {
            throw new IllegalArgumentException(mParameterName + " must be larger than or equal to " + mOtherInteger + ".");
        }

        return mInteger;
    }
}