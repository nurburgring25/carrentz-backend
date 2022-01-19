package dev.burikk.carrentz.app.entity;

import dev.burikk.carrentz.engine.datasource.annotation.*;
import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.entity.annotation.MarkAuditable;

import java.math.BigDecimal;
import java.sql.JDBCType;

/**
 * @author Muhammad Irfan
 * @since 19/01/2022 09.51
 */
@MarkAuditable
@MarkTable("vehicles")
@PrimaryKeyConstraint("vehicles_pk")
@UniqueKeyConstraint(id = 1, value = "vehicles_uk")
public class VehicleEntity extends Entity {
    @PrimaryKey
    @MarkColumn(
            value = "id",
            jdbcType = JDBCType.ROWID,
            isNotNull = true
    )
    private Long id;

    @MarkColumn(
            value = "store_id",
            jdbcType = JDBCType.BIGINT,
            isNotNull = true,
            columnReference = "id",
            tableReference = "stores"
    )
    private Long storeId;

    @MarkColumn(
            value = "vehicle_type_id",
            jdbcType = JDBCType.BIGINT,
            isNotNull = true,
            columnReference = "id",
            tableReference = "vehicle_types"
    )
    private Long vehicleTypeId;

    @UniqueKey(1)
    @MarkColumn(
            value = "license_number",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 16,
            isNotNull = true
    )
    private String licenseNumber;

    @MarkColumn(
            value = "name",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 64,
            isNotNull = true
    )
    private String name;

    @MarkColumn(
            value = "description",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 256,
            isNotNull = true
    )
    private String description;

    @MarkColumn(
            value = "cost_per_day",
            jdbcType = JDBCType.NUMERIC,
            isNotNull = true
    )
    private BigDecimal costPerDay;

    @MarkColumn(
            value = "late_return_fine_per_day",
            jdbcType = JDBCType.NUMERIC,
            isNotNull = true
    )
    private BigDecimal lateReturnFinePerDay;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStoreId() {
        return this.storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getVehicleTypeId() {
        return this.vehicleTypeId;
    }

    public void setVehicleTypeId(Long vehicleTypeId) {
        this.vehicleTypeId = vehicleTypeId;
    }

    public String getLicenseNumber() {
        return this.licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getCostPerDay() {
        return this.costPerDay;
    }

    public void setCostPerDay(BigDecimal costPerDay) {
        this.costPerDay = costPerDay;
    }

    public BigDecimal getLateReturnFinePerDay() {
        return this.lateReturnFinePerDay;
    }

    public void setLateReturnFinePerDay(BigDecimal lateReturnFinePerDay) {
        this.lateReturnFinePerDay = lateReturnFinePerDay;
    }
}