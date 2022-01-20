package dev.burikk.carrentz.engine.datasource;

import dev.burikk.carrentz.engine.common.WynixResult;
import dev.burikk.carrentz.engine.common.WynixResults;
import dev.burikk.carrentz.engine.datasource.enumeration.JoinType;
import dev.burikk.carrentz.engine.util.Parameters;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.naming.NamingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Muhammad Irfan
 * @since 12/3/2017 12:38 PM
 */
public class DMLAssembler {
    private List<String> mSelects;
    private List<String> mDistincts;
    private List<String> mFroms;
    private List<String> mJoins;
    private List<String> mGroups;
    private List<String> mOrders;
    private List<String> mWheres;

    private int mLimit;
    private String mHaving;
    private List<Object> mParameters;

    {
        this.mSelects = new ArrayList<>();
        this.mDistincts = new ArrayList<>();
        this.mFroms = new ArrayList<>();
        this.mJoins = new ArrayList<>();
        this.mGroups = new ArrayList<>();
        this.mOrders = new ArrayList<>();
        this.mWheres = new ArrayList<>();

        this.mParameters = new ArrayList<>();
    }

    public DMLAssembler() {}

    public DMLAssembler select(@NotNull String... mColumns) {
        Parameters.requireNotNull(mColumns, "mColumns");

        this.mSelects.addAll(Arrays.asList(mColumns));

        return this;
    }

    public DMLAssembler select(@NotNull Boolean mCondition, @NotNull String... mColumns) {
        Parameters.requireNotNull(mCondition, "mCondition");
        Parameters.requireNotNull(mColumns, "mColumns");

        if (mCondition) {
            this.mSelects.addAll(Arrays.asList(mColumns));
        }

        return this;
    }

    public DMLAssembler distinct(@NotNull String... mColumns) {
        Parameters.requireNotNull(mColumns, "mColumns");

        this.mDistincts.addAll(Arrays.asList(mColumns));

        return this;
    }

    public DMLAssembler from(@NotNull String... mTables) {
        Parameters.requireNotNull(mTables, "mTables");

        this.mFroms.addAll(Arrays.asList(mTables));

        return this;
    }

    public DMLAssembler join(@NotNull String mJoinCommand, @NotNull JoinType mJoinType) {
        Parameters.requireNotNull(mJoinCommand, "mJoinCommand");
        Parameters.requireNotNull(mJoinType, "mJoinType");

        this.mJoins.add(mJoinType.getText() + " " + mJoinCommand);

        return this;
    }

    public DMLAssembler and() {
        this.mWheres.add("AND");

        return this;
    }

    public DMLAssembler and(@NotNull Boolean mCondition) {
        Parameters.requireNotNull(mCondition, "mCondition");

        if (mCondition) {
            this.mWheres.add("AND");
        }

        return this;
    }

    public DMLAssembler or() {
        this.mWheres.add("OR");

        return this;
    }

    public DMLAssembler or(@NotNull Boolean mCondition) {
        Parameters.requireNotNull(mCondition, "mCondition");

        if (mCondition) {
            this.mWheres.add("OR");
        }

        return this;
    }

    public DMLAssembler equalTo(@NotNull String mColumn, Object mParameter) {
        Parameters.requireNotNull(mColumn, "mColumn");

        if (mParameter != null) {
            this.mWheres.add(mColumn + " = ?");
            this.mParameters.add(mParameter);
        }

        return this;
    }

    public DMLAssembler equalTo(@NotNull String mColumn, Object mParameter, @NotNull Boolean mCondition) {
        Parameters.requireNotNull(mColumn, "mColumn");
        Parameters.requireNotNull(mCondition, "mCondition");

        if (mCondition) {
            if (mParameter != null) {
                this.mWheres.add(mColumn + " = ?");
                this.mParameters.add(mParameter);
            }
        }

        return this;
    }

    public DMLAssembler notEqualTo(@NotNull String mColumn, Object mParameter) {
        Parameters.requireNotNull(mColumn, "mColumn");

        if (mParameter != null) {
            this.mWheres.add(mColumn + " != ?");
            this.mParameters.add(mParameter);
        }

        return this;
    }

    public DMLAssembler notEqualTo(@NotNull String mColumn, Object mParameter, @NotNull Boolean mCondition) {
        Parameters.requireNotNull(mColumn, "mColumn");
        Parameters.requireNotNull(mCondition, "mCondition");

        if (mCondition) {
            if (mParameter != null) {
                this.mWheres.add(mColumn + " != ?");
                this.mParameters.add(mParameter);
            }
        }

        return this;
    }

    public DMLAssembler lessThan(@NotNull String mColumn, Object mParameter) {
        Parameters.requireNotNull(mColumn, "mColumn");

        if (mParameter != null) {
            this.mWheres.add(mColumn + " < ?");
            this.mParameters.add(mParameter);
        }

        return this;
    }

    public DMLAssembler lessThan(@NotNull String mColumn, Object mParameter, @NotNull Boolean mCondition) {
        Parameters.requireNotNull(mColumn, "mColumn");
        Parameters.requireNotNull(mCondition, "mCondition");

        if (mCondition) {
            if (mParameter != null) {
                this.mWheres.add(mColumn + " < ?");
                this.mParameters.add(mParameter);
            }
        }

        return this;
    }

    public DMLAssembler lessThanOrEqualTo(@NotNull String mColumn, Object mParameter) {
        Parameters.requireNotNull(mColumn, "mColumn");

        if (mParameter != null) {
            this.mWheres.add(mColumn + " <= ?");
            this.mParameters.add(mParameter);
        }

        return this;
    }

    public DMLAssembler lessThanOrEqualTo(@NotNull String mColumn, Object mParameter, @NotNull Boolean mCondition) {
        Parameters.requireNotNull(mColumn, "mColumn");
        Parameters.requireNotNull(mCondition, "mCondition");

        if (mCondition) {
            if (mParameter != null) {
                this.mWheres.add(mColumn + " <= ?");
                this.mParameters.add(mParameter);
            }
        }

        return this;
    }

    public DMLAssembler greaterThan(@NotNull String mColumn, Object mParameter) {
        Parameters.requireNotNull(mColumn, "mColumn");

        if (mParameter != null) {
            this.mWheres.add(mColumn + " > ?");
            this.mParameters.add(mParameter);
        }

        return this;
    }

    public DMLAssembler greaterThan(@NotNull String mColumn, Object mParameter, @NotNull Boolean mCondition) {
        Parameters.requireNotNull(mColumn, "mColumn");
        Parameters.requireNotNull(mCondition, "mCondition");

        if (mCondition) {
            if (mParameter != null) {
                this.mWheres.add(mColumn + " > ?");
                this.mParameters.add(mParameter);
            }
        }

        return this;
    }

    public DMLAssembler greaterThanOrEqualTo(@NotNull String mColumn, Object mParameter) {
        Parameters.requireNotNull(mColumn, "mColumn");

        if (mParameter != null) {
            this.mWheres.add(mColumn + " >= ?");
            this.mParameters.add(mParameter);
        }

        return this;
    }

    public DMLAssembler greaterThanOrEqualTo(@NotNull String mColumn, Object mParameter, @NotNull Boolean mCondition) {
        Parameters.requireNotNull(mColumn, "mColumn");
        Parameters.requireNotNull(mCondition, "mCondition");

        if (mCondition) {
            if (mParameter != null) {
                this.mWheres.add(mColumn + " >= ?");
                this.mParameters.add(mParameter);
            }
        }

        return this;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public DMLAssembler in(@NotNull String mColumn, Object... mParameters) {
        Parameters.requireNotNull(mColumn, "mColumn");
        Parameters.requireNotNull(mParameters, "mParameters");

        if (mParameters.length > 0) {
            StringBuilder mStringBuilder = new StringBuilder();

            mStringBuilder
                    .append(mColumn)
                    .append(" IN ( ")
                    .append(String.join(", ", Collections.nCopies(mParameters.length, "?")))
                    .append(" )");

            this.mWheres.add(mStringBuilder.toString());
            this.mParameters.addAll(Arrays.asList(mParameters));
        }

        return this;
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public DMLAssembler in(@NotNull String mColumn, @NotNull Boolean mCondition, Object... mParameters) {
        Parameters.requireNotNull(mColumn, "mColumn");
        Parameters.requireNotNull(mCondition, "mCondition");
        Parameters.requireNotNull(mParameters, "mParameters");

        if (mCondition) {
            if (mParameters.length > 0) {
                StringBuilder mStringBuilder = new StringBuilder();

                mStringBuilder
                        .append(mColumn)
                        .append(" IN ( ")
                        .append(String.join(", ", Collections.nCopies(mParameters.length, "?")))
                        .append(" )");

                this.mWheres.add(mStringBuilder.toString());
                this.mParameters.addAll(Arrays.asList(mParameters));
            }
        }

        return this;
    }

    public DMLAssembler isNotNull(@NotNull String mColumn) {
        Parameters.requireNotNull(mColumn, "mColumn");

        this.mWheres.add(mColumn + " IS NOT NULL");

        return this;
    }

    public DMLAssembler isNotNull(@NotNull String mColumn, @NotNull Boolean mCondition) {
        Parameters.requireNotNull(mColumn, "mColumn");
        Parameters.requireNotNull(mCondition, "mCondition");

        if (mCondition) {
            this.mWheres.add(mColumn + " IS NOT NULL");
        }

        return this;
    }

    public DMLAssembler isNull(@NotNull String mColumn) {
        Parameters.requireNotNull(mColumn, "mColumn");

        this.mWheres.add(mColumn + " IS NULL");

        return this;
    }

    public DMLAssembler isNull(@NotNull String mColumn, @NotNull Boolean mCondition) {
        Parameters.requireNotNull(mColumn, "mColumn");
        Parameters.requireNotNull(mCondition, "mCondition");

        if (mCondition) {
            this.mWheres.add(mColumn + " IS NULL");
        }

        return this;
    }

    public DMLAssembler customWhere(@NotNull String mClause) {
        Parameters.requireNotNull(mClause, "mClause");

        this.mWheres.add(mClause);

        return this;
    }

    public DMLAssembler customWhere(@NotNull String mClause, @NotNull Boolean mCondition) {
        Parameters.requireNotNull(mClause, "mClause");
        Parameters.requireNotNull(mCondition, "mCondition");

        if (mCondition) {
            this.mWheres.add(mClause);
        }

        return this;
    }

    public DMLAssembler groupBy(@NotNull String... mColumns) {
        Parameters.requireNotNull(mColumns, "mColumns");

        this.mGroups.addAll(Arrays.asList(mColumns));

        return this;
    }

    public DMLAssembler asc(@NotNull String mColumn) {
        Parameters.requireNotNull(mColumn, "mColumn");

        this.mOrders.add(mColumn + " ASC");

        return this;
    }

    public DMLAssembler desc(@NotNull String mColumn) {
        Parameters.requireNotNull(mColumn, "mColumn");

        this.mOrders.add(mColumn + " DESC");

        return this;
    }

    public DMLAssembler limit(int mLimit) {
        Parameters.requireLargerThanTo(mLimit, 0, "mLimit");

        this.mLimit = mLimit;

        return this;
    }

    public DMLAssembler having(@NotNull String mHaving) {
        Parameters.requireNotNull(mHaving, "mHaving");

        this.mHaving = mHaving;

        return this;
    }

    public DMLAssembler addParameter(Object mParameter, Boolean mCondition) {
        if (mCondition) {
            this.mParameters.add(mParameter);
        }

        return this;
    }

    public DMLAssembler addParameter(Object mParameter) {
        this.mParameters.add(mParameter);

        return this;
    }

    public String build() {
        StringBuilder mStringBuilder = new StringBuilder();

        mStringBuilder.append("SELECT ");

        if (!this.mDistincts.isEmpty()) {
            mStringBuilder
                    .append("DISTINCT ON ( ")
                    .append(String.join(", ", this.mDistincts))
                    .append(" ) ");
        }

        mStringBuilder
                .append(String.join(", ", this.mSelects))
                .append(" FROM ")
                .append(String.join(", ", this.mFroms));

        if (!this.mJoins.isEmpty()) {
            mStringBuilder
                    .append(" ")
                    .append(String.join(" ", this.mJoins));
        }

        if (!this.mWheres.isEmpty()) {
            mStringBuilder
                    .append(" WHERE ")
                    .append(String.join(" ", this.mWheres));
        }

        if (!this.mGroups.isEmpty()) {
            mStringBuilder
                    .append(" GROUP BY ")
                    .append(String.join(", ", this.mGroups));
        }

        if (StringUtils.isNotEmpty(this.mHaving)) {
            mStringBuilder
                    .append(" HAVING ")
                    .append(this.mHaving);
        }

        if (!this.mOrders.isEmpty()) {
            mStringBuilder
                    .append(" ORDER BY ")
                    .append(String.join(", ", this.mOrders));
        }

        if (mLimit > 0) {
            mStringBuilder
                    .append(" LIMIT ")
                    .append(mLimit);
        }

        mStringBuilder.append(";");

        return mStringBuilder.toString();
    }

    public List<Object> getParameters() {
        return this.mParameters;
    }

    public <T extends WynixResult> WynixResults<T> getWynixResults(
            @Null String mDataSourceName,
            @NotNull Class<T> mClass
    ) throws SQLException, InstantiationException, IllegalAccessException, NamingException {
        Parameters.requireNotNull(mClass, "mClass");

        return DMLManager.getWynixResultsFromQuery(mDataSourceName, this.build(), mClass, this.mParameters.toArray());
    }

    public <T extends WynixResult> WynixResults<T> getWynixResults(
            @NotNull Class<T> mClass
    ) throws SQLException, NamingException, InstantiationException, IllegalAccessException {
        Parameters.requireNotNull(mClass, "mClass");

        return DMLManager.getWynixResultsFromQuery(this.build(), mClass, this.mParameters.toArray());
    }

    public <T extends WynixResult> T getWynixResult(
            @Null String mDataSourceName,
            @NotNull Class<T> mClass
    ) throws SQLException, InstantiationException, IllegalAccessException, NamingException {
        Parameters.requireNotNull(mClass, "mClass");

        return DMLManager.getWynixResultFromQuery(mDataSourceName, this.build(), mClass, this.mParameters.toArray());
    }

    public <T extends WynixResult> T getWynixResult(
            @NotNull Class<T> mClass
    ) throws SQLException, NamingException, InstantiationException, IllegalAccessException {
        Parameters.requireNotNull(mClass, "mClass");

        return DMLManager.getWynixResultFromQuery(this.build(), mClass, this.mParameters.toArray());
    }

    public <T> List<T> getObjects(
            @Null String mDataSourceName
    ) throws SQLException, NamingException {
        return DMLManager.getObjectsFromQuery(mDataSourceName, this.build(), this.mParameters.toArray());
    }

    public <T> List<T> getObjects() throws SQLException, NamingException {
        return DMLManager.getObjectsFromQuery(this.build(), this.mParameters.toArray());
    }

    public <T> T getObject(
            @Null String mDataSourceName
    ) throws SQLException, NamingException {
        return DMLManager.getObjectFromQuery(mDataSourceName, this.build(), this.mParameters.toArray());
    }

    public <T> T getObject() throws SQLException, NamingException {
        return DMLManager.getObjectFromQuery(this.build(), this.mParameters.toArray());
    }

    public <T> List<T> getEncryptedObjects(
            @Null String mDataSourceName,
            @NotNull Class<T> mType
    ) throws NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, SQLException, NamingException, InvalidKeyException {
        return DMLManager.getEncryptedObjectsFromQuery(mDataSourceName, this.build(), mType, this.mParameters.toArray());
    }

    public <T> List<T> getEncryptedObjects(
            @NotNull Class<T> mType
    ) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, SQLException, BadPaddingException, NamingException, InvalidAlgorithmParameterException {
        return DMLManager.getEncryptedObjectsFromQuery(this.build(), mType, this.mParameters.toArray());
    }

    public <T> T getEncryptedObject(
            @Null String mDataSourceName,
            @NotNull Class<T> mType
    ) throws NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, SQLException, NamingException, InvalidKeyException {
        return DMLManager.getEncryptedObjectFromQuery(mDataSourceName, this.build(), mType, this.mParameters.toArray());
    }

    public <T> T getEncryptedObject(
            @NotNull Class<T> mType
    ) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, SQLException, BadPaddingException, NamingException, InvalidAlgorithmParameterException {
        return DMLManager.getEncryptedObjectFromQuery(this.build(), mType, this.mParameters.toArray());
    }

    public static DMLAssembler create() {
        return new DMLAssembler();
    }
}