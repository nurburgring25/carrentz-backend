package dev.burikk.carrentz.engine.datasource;

import dev.burikk.carrentz.engine.common.WynixResult;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Muhammad Irfan
 * @since 09/01/2019 23.14
 */
public class LOVItem implements WynixResult {
    private Object identity;
    private String description;
    private Map<String, Object> options;

    {
        this.options = new HashMap<>();
    }

    public LOVItem() {}

    public LOVItem(
            Object identity,
            String description,
            Map<String, Object> options
    ) {
        this.identity = identity;
        this.description = description;

        if (options != null) {
            this.options = options;
        }
    }

    public Object getIdentity() {
        return this.identity;
    }

    public void setIdentity(Object identity) {
        this.identity = identity;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getOptions() {
        return this.options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }
}