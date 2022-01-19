package dev.burikk.carrentz.engine.util;

import dev.burikk.carrentz.engine.datasource.enumeration.Platform;
import dev.burikk.carrentz.engine.datasource.exception.UndefinedJDBCTypeMappingException;
import dev.burikk.carrentz.engine.datasource.exception.UndefinedJavaTypeMappingException;
import dev.burikk.carrentz.engine.datasource.exception.UnsupportedDataTypeException;
import com.sun.istack.internal.NotNull;

import java.io.BufferedInputStream;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * @author Muhammad Irfan
 * @since 23/08/2017 8:49
 */
public class DataTypes {
    public static JDBCType getJDBCType(@NotNull Class<?> mJavaType) {
        Parameters.requireNotNull(mJavaType, "mJavaType");

        if (Objects.equals(mJavaType, Byte.class)) {
            return JDBCType.TINYINT;
        } else if (Objects.equals(mJavaType, Short.class)) {
            return JDBCType.SMALLINT;
        } else if (Objects.equals(mJavaType, Integer.class)) {
            return JDBCType.INTEGER;
        } else if (Objects.equals(mJavaType, Long.class)) {
            return JDBCType.BIGINT;
        } else if (Objects.equals(mJavaType, Float.class)) {
            return JDBCType.REAL;
        } else if (Objects.equals(mJavaType, Double.class)) {
            return JDBCType.DOUBLE;
        } else if (Objects.equals(mJavaType, BigDecimal.class)) {
            return JDBCType.NUMERIC;
        } else if (Objects.equals(mJavaType, Boolean.class)) {
            return JDBCType.BIT;
        } else if (Objects.equals(mJavaType, String.class)) {
            return JDBCType.VARCHAR;
        } else if (Objects.equals(mJavaType, String.class)) {
            return JDBCType.LONGVARCHAR;
        } else if (Objects.equals(mJavaType, byte[].class)) {
            return JDBCType.VARBINARY;
        } else if (Objects.equals(mJavaType, BufferedInputStream.class)) {
            return JDBCType.LONGVARBINARY;
        } else if (Objects.equals(mJavaType, LocalDate.class)) {
            return JDBCType.DATE;
        } else if (Objects.equals(mJavaType, LocalTime.class)) {
            return JDBCType.TIME;
        } else if (Objects.equals(mJavaType, LocalDateTime.class)) {
            return JDBCType.TIMESTAMP;
        } else if (Objects.equals(mJavaType, Clob.class)) {
            return JDBCType.CLOB;
        } else if (Objects.equals(mJavaType, Blob.class)) {
            return JDBCType.BLOB;
        } else {
            throw new UndefinedJavaTypeMappingException(mJavaType);
        }
    }

    public static Class<?> getJavaType(@NotNull JDBCType mJDBCType) {
        Parameters.requireNotNull(mJDBCType, "mJDBCType");

        switch (mJDBCType) {
            case TINYINT:
                return Byte.class;
            case SMALLINT:
                return Short.class;
            case INTEGER:
                return Integer.class;
            case BIGINT:
                return Long.class;
            case REAL:
                return Float.class;
            case DOUBLE:
                return Double.class;
            case NUMERIC:
                return BigDecimal.class;
            case BIT:
                return Boolean.class;
            case VARCHAR:
                return String.class;
            case LONGVARCHAR:
                return String.class;
            case BINARY:
                return byte[].class;
            case VARBINARY:
                return byte[].class;
            case LONGVARBINARY:
                return BufferedInputStream.class;
            case DATE:
                return LocalDate.class;
            case TIME:
                return LocalTime.class;
            case TIMESTAMP:
                return LocalDateTime.class;
            case CLOB:
                return Clob.class;
            case BLOB:
                return Blob.class;
            case ROWID:
                return Long.class;
            default:
                throw new UndefinedJDBCTypeMappingException(mJDBCType);
        }
    }

    public static String getName(
            @NotNull JDBCType mJDBCType,
            @NotNull Platform mPlatform,
            int mMaxLength
    ) {
        Parameters.requireNotNull(mJDBCType, "mJDBCType");
        Parameters.requireNotNull(mPlatform, "mPlatform");
        Parameters.requireLargerThanOrEqualTo(mMaxLength, 0, "mMaxLength");

        outerSwitch:
        switch (mPlatform) {
            case POSTGRESQL:
                switch (mJDBCType) {
                    case ARRAY:
                        return "JSON";
                    case BIGINT:
                        return "BIGINT";
                    case BINARY:
                        return "BYTEA";
                    case BIT:
                        return "BOOLEAN";
                    case CHAR:
                        return "CHAR" + ((mMaxLength != 0) ? "(" + mMaxLength + ")" : "");
                    case DATE:
                        return "DATE";
                    case DOUBLE:
                        return "FLOAT8";
                    case INTEGER:
                        return "INTEGER";
                    case NUMERIC:
                        return "NUMERIC" + ((mMaxLength != 0) ? "(" + mMaxLength + ")" : "");
                    case OTHER:
                        return "JSON";
                    case REAL:
                        return "FLOAT4";
                    case SMALLINT:
                        return "INT2";
                    case SQLXML:
                        return "XML";
                    case TIME:
                        return "TIME";
                    case TIMESTAMP:
                        return "TIMESTAMP";
                    case TIME_WITH_TIMEZONE:
                        return "TIME WITH TIME ZONE";
                    case TIMESTAMP_WITH_TIMEZONE:
                        return "TIMESTAMP WITH TIME ZONE";
                    case VARCHAR:
                        return "VARCHAR" + ((mMaxLength != 0) ? "(" + mMaxLength + ")" : "");
                    case LONGVARCHAR:
                        return "TEXT";
                    case ROWID:
                        return "BIGSERIAL";
                    default:
                        break outerSwitch;
                }
            case MYSQL:
                switch (mJDBCType) {
                    case BIGINT:
                        return "BIGINT";
                    case BINARY:
                        return "BINARY";
                    case BIT:
                        return "BOOL";
                    case CHAR:
                        return "CHAR";
                    case DATE:
                        return "DATE";
                    case DECIMAL:
                        return "DECIMAL";
                    case DOUBLE:
                        return "DOUBLE";
                    case INTEGER:
                        return "INTEGER";
                    case LONGVARBINARY:
                        return "LONG VARBINARY";
                    case LONGVARCHAR:
                        return "TEXT";
                    case NUMERIC:
                        return "NUMERIC";
                    case REAL:
                        return "FLOAT";
                    case SMALLINT:
                        return "SMALLINT";
                    case TIME:
                        return "TIME";
                    case TIMESTAMP:
                        return "TIMESTAMP";
                    case TINYINT:
                        return "TINYINT";
                    case VARBINARY:
                        return "VARBINARY(255)";
                    case BLOB:
                        return "BLOB";
                    case VARCHAR:
                        return "VARCHAR(255)";
                    default:
                        break outerSwitch;
                }
            case SQLSERVER:
                switch (mJDBCType) {
                    case BIGINT:
                        return "bigint";
                    case BINARY:
                        return "binary";
                    case BIT:
                        return "char";
                    case CHAR:
                        return "char";
                    case DATE:
                        return "date";
                    case DECIMAL:
                        return "decimal";
                    case DOUBLE:
                        return "float";
                    case INTEGER:
                        return "int";
                    case LONGNVARCHAR:
                        return "xml";
                    case LONGVARBINARY:
                        return "image";
                    case LONGVARCHAR:
                        return "text";
                    case NCHAR:
                        return "nchar";
                    case NUMERIC:
                        return "numeric";
                    case NVARCHAR:
                        return "nvarchar";
                    case REAL:
                        return "real";
                    case SMALLINT:
                        return "smallint";
                    case TIME:
                        return "time";
                    case TIMESTAMP:
                        return "datetime";
                    case TINYINT:
                        return "tinyint";
                    case VARBINARY:
                        return "varbinary";
                    case VARCHAR:
                        return "varchar(255)";
                    default:
                        break outerSwitch;
                }
        }

        throw new UnsupportedDataTypeException(mPlatform, mJDBCType);
    }

    public static String getName(
            @NotNull JDBCType mJDBCType,
            @NotNull Platform mPlatform
    ) {
        return getName(mJDBCType, mPlatform, 0);
    }

    public static Object convert(Object mObject) {
        if (mObject != null) {
            if (mObject instanceof Timestamp) {
                mObject = ((Timestamp) mObject).toLocalDateTime();
            } else if (mObject instanceof LocalDateTime) {
                mObject = Timestamp.valueOf((LocalDateTime) mObject);
            } else if (mObject instanceof Date) {
                mObject = ((Date) mObject).toLocalDate();
            } else if (mObject instanceof LocalDate) {
                mObject = Date.valueOf((LocalDate) mObject);
            } else if (mObject instanceof Time) {
                mObject = ((Time) mObject).toLocalTime();
            } else if (mObject instanceof LocalTime) {
                mObject = Time.valueOf((LocalTime) mObject);
            }
        }

        return mObject;
    }

    public static long getEpochMilli(@NotNull LocalDateTime mLocalDateTime) {
        Parameters.requireNotNull(mLocalDateTime, "mLocalDateTime");

        return mLocalDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static long getEpochMilli(@NotNull LocalDate mLocalDate) {
        Parameters.requireNotNull(mLocalDate, "mLocalDate");

        return getEpochMilli(LocalDateTime.of(mLocalDate, LocalTime.MIDNIGHT));
    }

    public static long getEpochMilli(@NotNull LocalTime mLocalTime) {
        Parameters.requireNotNull(mLocalTime, "mLocalTime");

        return getEpochMilli(LocalDateTime.of(LocalDate.now(), mLocalTime));
    }

    public static String decodeURL(String encodedURL) {
        String result = "";
        try {
            result = URLDecoder.decode(encodedURL, "UTF-8");
        } catch (Exception ignored) {}

        return result;
    }
}