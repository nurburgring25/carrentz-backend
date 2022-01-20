package dev.burikk.carrentz.engine.datasource;

import dev.burikk.carrentz.engine.datasource.enumeration.Operator;
import dev.burikk.carrentz.engine.util.Parameters;
import javax.validation.constraints.NotNull;

import java.util.List;

/**
 * @author Muhammad Irfan
 * @since 12/3/2017 10:20 AM
 */
public class Matcher {
    //<editor-fold desc="Property">
    private int mID;
    private String mName;
    private Operator mOperator;
    private List<String> mValues;
    //</editor-fold>

    public Matcher(
            @NotNull int mID,
            @NotNull String mName,
            @NotNull Operator mOperator,
            List<String> mValues
    ) {
        Parameters.requireNotNull(mID, "mID");
        Parameters.requireNotNull(mName, "mName");
        Parameters.requireNotNull(mOperator, "mOperator");

        if (!(mOperator == Operator.IS_NOT_NULL || mOperator == Operator.IS_NULL)) {
            if (mValues == null || mValues.isEmpty()) {
                throw new IllegalArgumentException("mValues cannot be null.");
            }
        }

        this.mID = mID;
        this.mName = mName;
        this.mOperator = mOperator;
        this.mValues = mValues;
    }

    //<editor-fold desc="Getter">
    public int getID() {
        return this.mID;
    }

    public String getName() {
        return this.mName;
    }

    public Operator getOperator() {
        return this.mOperator;
    }

    public List<String> getValues() {
        return this.mValues;
    }
    //</editor-fold>
}