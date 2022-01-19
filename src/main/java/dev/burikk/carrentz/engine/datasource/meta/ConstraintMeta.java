package dev.burikk.carrentz.engine.datasource.meta;

import dev.burikk.carrentz.engine.util.Parameters;
import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Muhammad Irfan
 * @since 23/08/2017 10:49
 */
public class ConstraintMeta {
    //<editor-fold desc="Property">
    private String mName;
    private final List<String> mColumnNames;
    //</editor-fold>

    {
        this.mColumnNames = new ArrayList<>();
    }

    ConstraintMeta(
            @NotNull String mName,
            @NotNull String... mColumnNames
    ) {
        Parameters.requireNotNull(mName, "mName");
        Parameters.requireNotNull(mColumnNames, "mColumnNames");

        if (mColumnNames.length <= 0) {
            throw new IllegalArgumentException("At least one column must be provided to create constraint.");
        }

        this.mName = mName;
        Collections.addAll(this.mColumnNames, mColumnNames);
    }

    //<editor-fold desc="Getter">
    public String getName() {
        return mName;
    }

    public List<String> getColumnNames() {
        return mColumnNames;
    }
    //</editor-fold>
}