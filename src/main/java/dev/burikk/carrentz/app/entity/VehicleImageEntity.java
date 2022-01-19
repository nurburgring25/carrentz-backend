package dev.burikk.carrentz.app.entity;

import dev.burikk.carrentz.engine.datasource.annotation.*;
import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.entity.annotation.MarkAuditable;

import java.sql.JDBCType;

/**
 * @author Muhammad Irfan
 * @since 19/01/2022 09.51
 */
@MarkAuditable
@MarkTable("vehicle_images")
@PrimaryKeyConstraint("vehicle_images_pk")
public class VehicleImageEntity extends Entity {
    @PrimaryKey
    @MarkColumn(
            value = "id",
            jdbcType = JDBCType.ROWID,
            isNotNull = true
    )
    private Long id;

    @MarkColumn(
            value = "vehicle_id",
            jdbcType = JDBCType.BIGINT,
            isNotNull = true,
            columnReference = "id",
            tableReference = "vehicles"
    )
    private Long vehicleId;

    @MarkColumn(
            value = "image",
            jdbcType = JDBCType.BINARY,
            isNotNull = true
    )
    private byte[] image;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVehicleId() {
        return this.vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public byte[] getImage() {
        return this.image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}