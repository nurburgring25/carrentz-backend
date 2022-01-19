package dev.burikk.carrentz.engine.datasource;

import dev.burikk.carrentz.engine.util.Parameters;
import com.sun.istack.internal.NotNull;

import java.lang.reflect.Field;
import java.sql.JDBCType;

/**
 * @author Muhammad Irfan
 * @since 23/08/2017 8:41
 */
public class BaseColumn {
    //<editor-fold desc="Property">
    private String mName;
    private JDBCType mJDBCType;
    private Field mField;
    //</editor-fold>

    BaseColumn(
            @NotNull String mName,
            @NotNull JDBCType mJDBCType,
            @NotNull Field mField
    ) {
        Parameters.requireNotNull(mName, "mName");
        Parameters.requireNotNull(mJDBCType, "mJDBCType");
        Parameters.requireNotNull(mField, "mField");

        this.mName = mName;
        this.mJDBCType = mJDBCType;
        this.mField = mField;
    }

    //<editor-fold desc="Getter and setter">
    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public JDBCType getJDBCType() {
        return mJDBCType;
    }

    public void setJDBCType(JDBCType mJDBCType) {
        this.mJDBCType = mJDBCType;
    }

    public Field getField() {
        return mField;
    }

    public void setField(Field mField) {
        this.mField = mField;
    }
    //</editor-fold>
}
