package dev.burikk.carrentz.engine.datasource;

import dev.burikk.carrentz.engine.util.Parameters;
import com.sun.istack.internal.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Muhammad Irfan
 * @since 23/08/2017 11:21
 */
public class Constraint {
    //<editor-fold desc="Property">
    private String mName;
    private Map<Field, Column> mMap;
    private Map<Column, List<Matcher>> mMatcherMap;
    //</editor-fold>

    {
        this.mMap = new HashMap<>();
        this.mMatcherMap = new HashMap<>();
    }

    public Constraint(
            @NotNull String mName
    ) {
        Parameters.requireNotNull(mName, "mName");

        this.mName = mName;
    }

    //<editor-fold desc="Getter">
    public String getName() {
        return mName;
    }

    public Map<Field, Column> getMap() {
        return this.mMap;
    }

    public Map<Column, List<Matcher>> getMatcherMap() {
        return this.mMatcherMap;
    }

    public List<String> getColumnNames() {
        return this.mMap.entrySet()
                .stream()
                .map(mEntry -> mEntry.getValue().getName())
                .collect(Collectors.toList());
    }

    public List<Field> getFields() {
        return this.mMap.entrySet()
                .stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    //</editor-fold>
}