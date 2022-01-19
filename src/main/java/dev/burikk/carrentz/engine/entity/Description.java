package dev.burikk.carrentz.engine.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Muhammad Irfan
 * @since 28/08/2017 10:42
 */
public class Description {
    private String fieldName;
    private String fieldDescription;
    private boolean fieldVisible;

    @JsonIgnore
    private boolean used;
    @JsonIgnore
    private String key;

    {
        this.fieldDescription = "Untitled";
        this.fieldVisible = true;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldDescription() {
        return this.fieldDescription;
    }

    public void setFieldDescription(String fieldDescription) {
        this.fieldDescription = fieldDescription;
    }

    public boolean isFieldVisible() {
        return this.fieldVisible;
    }

    public void setFieldVisible(boolean fieldVisible) {
        this.fieldVisible = fieldVisible;
    }

    public boolean isUsed() {
        return this.used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}