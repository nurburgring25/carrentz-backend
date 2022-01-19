package dev.burikk.carrentz.app.entity;

import dev.burikk.carrentz.engine.datasource.annotation.*;
import dev.burikk.carrentz.engine.entity.Entity;

import java.sql.JDBCType;

/**
 * @author Muhammad Irfan
 * @since 19/01/2022 09.51
 */
@MarkTable("vehicle_types")
@PrimaryKeyConstraint("vehicle_types_pk")
@UniqueKeyConstraint(id = 1, value = "vehicle_type_name_uk")
public class VehicleTypeEntity extends Entity {
    @PrimaryKey
    @MarkColumn(
            value = "id",
            jdbcType = JDBCType.ROWID,
            isNotNull = true
    )
    private Long id;

    @UniqueKey(1)
    @MarkColumn(
            value = "name",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 64,
            isNotNull = true
    )
    private String name;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}