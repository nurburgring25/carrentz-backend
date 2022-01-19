package dev.burikk.carrentz.engine.common;

import dev.burikk.carrentz.engine.datasource.annotation.MarkColumn;
import dev.burikk.carrentz.engine.datasource.annotation.PrimaryKey;
import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.entity.annotation.MarkAuditable;

import java.sql.JDBCType;

/**
 * @author Muhammad Irfan
 * @since 19/04/2019 17.13
 */
@MarkAuditable
public class SessionEntity extends Entity implements WynixUser {
    @PrimaryKey
    @MarkColumn(
            value = "id",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 64,
            isNotNull = true
    )
    private String id;

    @MarkColumn(
            value = "password",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 256,
            isNotNull = true
    )
    private String password;

    @MarkColumn(
            value = "name",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 128,
            isNotNull = true
    )
    private String name;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getIdentity() {
        return this.getId();
    }
}