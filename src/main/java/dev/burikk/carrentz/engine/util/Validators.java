package dev.burikk.carrentz.engine.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import dev.burikk.carrentz.engine.exception.WynixException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import javax.validation.constraints.NotNull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Muhammad Irfan
 * @since 8/30/2017 6:41 PM
 */
public class Validators {
    public static transient String SPECIAL_CHARACTER_REGEX = "^[a-zA-Z0-9-%!/.,@\\s\\[\\]\\w()]+$";

    public static void validate(Object mObject, String mMessage) throws WynixException {
        if(mObject==null) {
            throw new WynixException(mMessage);
        } else {
            if(mObject instanceof String) {
                if(StringUtils.isBlank((String) mObject)) {
                    throw new WynixException(mMessage);
                }
            } else if(mObject instanceof Boolean) {
                if((Boolean) mObject) {
                    throw new WynixException(mMessage);
                }
            }
        }
    }

    public static void validate(Object mObject, String mMessage, Integer mCode) throws WynixException {
        if(mObject instanceof String) {
            if(StringUtils.isBlank((String) mObject)) {
                throw new WynixException(mMessage);
            }
        } else if(mObject instanceof Boolean) {
            if((Boolean) mObject) {
                throw new WynixException(mMessage, mCode);
            }
        }
    }

    public static void mandatory(Object mData, @NotNull String mFieldName) {
        mandatory(mData, mFieldName, null);
    }

    public static void mandatory(Object mData, @NotNull String mFieldName, Integer mIndex) {
        if (mData instanceof String) {
            validate(StringUtils.isBlank((String) mData), mFieldName + " is required" + (mIndex != null ? " at row " + mIndex : "") + ".");
        } else {
            validate(mData == null, mFieldName + " is required" + (mIndex != null ? " at row " + mIndex : "") + ".");
        }
    }

    public static void greaterThan(Number mData, Number mComparator, @NotNull String mFieldName) {
        greaterThan(mData, mComparator, mFieldName, null);
    }

    public static void greaterThan(Number mData, Number mComparator, @NotNull String mFieldName, Integer mIndex) {
        if (mData != null && mComparator != null) {
            validate(!(mData.longValue() > mComparator.longValue()), " Field " + mFieldName.toLowerCase() + " must be greater than " + mComparator.toString() + (mIndex != null ? " at row " + mIndex : "") + ".");
        }
    }

    public static void lessThan(Number mData, Number mComparator, @NotNull String mFieldName) {
        lessThan(mData, mComparator, mFieldName, null);
    }

    public static void lessThan(Number mData, Number mComparator, @NotNull String mFieldName, Integer mIndex) {
        if (mData != null && mComparator != null) {
            validate(!(mData.longValue() < mComparator.longValue()), " Field " + mFieldName.toLowerCase() + " must be less than " + mComparator.toString() + (mIndex != null ? " at row " + mIndex : "") + ".");
        }
    }

    public static void greaterThanOrEqualTo(Number mData, Number mComparator, @NotNull String mFieldName) {
        greaterThan(mData, mComparator, mFieldName, null);
    }

    public static void greaterThanOrEqualTo(Number mData, Number mComparator, @NotNull String mFieldName, Integer mIndex) {
        if (mData != null && mComparator != null) {
            validate(!(mData.longValue() >= mComparator.longValue()), " Field " + mFieldName.toLowerCase() + " must be greater than or equal to " + mComparator.toString() + (mIndex != null ? " at row " + mIndex : "") + ".");
        }
    }

    public static void lessThanOrEqualTo(Number mData, Number mComparator, @NotNull String mFieldName) {
        lessThanOrEqualTo(mData, mComparator, mFieldName, null);
    }

    public static void lessThanOrEqualTo(Number mData, Number mComparator, @NotNull String mFieldName, Integer mIndex) {
        if (mData != null && mComparator != null) {
            validate(!(mData.longValue() <= mComparator.longValue()), " Field " + mFieldName.toLowerCase() + " must be less than or equal to " + mComparator.toString() + (mIndex != null ? " at row " + mIndex : "") + ".");
        }
    }

    public static void detectSpecialCharacter(String mData, @NotNull String mFieldName) {
        detectSpecialCharacter(mData, mFieldName, null);
    }

    public static void detectSpecialCharacter(String mData, @NotNull String mFieldName, Integer mIndex) {
        if (StringUtils.isNotEmpty(mData)) {
            Pattern mPattern = Pattern.compile(SPECIAL_CHARACTER_REGEX);

            Matcher mMatcher = mPattern.matcher(mData);

            validate(!mMatcher.find(), "There is a character(s) that are not allowed at field " + mFieldName + (mIndex != null ? " at row " + mIndex : "") + ".");
        }
    }

    public static void isEmailValid(String mData, @NotNull String mFieldName) {
        isEmailValid(mData, mFieldName, null);
    }

    public static void isEmailValid(String mData, @NotNull String mFieldName, Integer mIndex) {
        if (mData != null) {
            Pattern mPattern = Pattern.compile("^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$");

            Matcher mMatcher = mPattern.matcher(mData);

            validate(!mMatcher.find(), mFieldName + " tidak sesuai" + (mIndex != null ? " pada baris " + mIndex : "") + ".");
            validate(!EmailValidator.getInstance().isValid(mData), mFieldName + " tidak sesuai" + (mIndex != null ? " pada baris " + mIndex : "") + ".");
        }
    }

    public static void isPhoneValid(String mData, @NotNull String mFieldName) {
        isPhoneValid(mData, mFieldName, null);
    }

    public static void isPhoneValid(String mData, @NotNull String mFieldName, Integer mIndex) {
        if (mData != null) {
            Pattern mPattern = Pattern.compile("^[0-9+]+$");

            Matcher mMatcher = mPattern.matcher(mData);

            validate(!mMatcher.find(), mFieldName + " tidak sesuai" + (mIndex != null ? " pada baris " + mIndex : "") + ".");

            PhoneNumberUtil mPhoneNumberUtil = PhoneNumberUtil.getInstance();
            try {
                Phonenumber.PhoneNumber mPhoneNumber = mPhoneNumberUtil.parseAndKeepRawInput(mData, "ID");

                validate(!mPhoneNumberUtil.isValidNumberForRegion(mPhoneNumber, "ID"), mFieldName + " tidak sesuai" + (mIndex != null ? " pada baris " + mIndex : "") + ".");
            } catch (NumberParseException ex) {
                throw new WynixException(mFieldName + " tidak sesuai" + (mIndex != null ? " pada baris " + mIndex : "") + ".");
            }
        }
    }
}