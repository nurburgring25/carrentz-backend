package dev.burikk.carrentz.app.entity;

import dev.burikk.carrentz.engine.datasource.annotation.*;
import dev.burikk.carrentz.engine.datasource.enumeration.JoinType;
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
@Reference(id = 1, sourceColumn = "store_id", targetTable = "stores", joinType = JoinType.INNER_JOIN)
@Reference(id = 2, sourceColumn = "vehicle_type_id", targetTable = "vehicle_types", joinType = JoinType.INNER_JOIN)
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

    @MarkReferencedColumn(
            value = "name",
            jdbcType = JDBCType.VARCHAR,
            referenceID = 1,
            alias = "store_name"
    )
    private String storeName;

    @MarkColumn(
            value = "vehicle_type_id",
            jdbcType = JDBCType.BIGINT,
            isNotNull = true,
            columnReference = "id",
            tableReference = "vehicle_types"
    )
    private Long vehicleTypeId;

    @MarkReferencedColumn(
            value = "name",
            jdbcType = JDBCType.VARCHAR,
            referenceID = 2,
            alias = "vehicle_type_name"
    )
    private String vehicleTypeName;

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
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public Long getVehicleTypeId() {
        return vehicleTypeId;
    }

    public void setVehicleTypeId(Long vehicleTypeId) {
        this.vehicleTypeId = vehicleTypeId;
    }

    public String getVehicleTypeName() {
        return vehicleTypeName;
    }

    public void setVehicleTypeName(String vehicleTypeName) {
        this.vehicleTypeName = vehicleTypeName;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getCostPerDay() {
        return costPerDay;
    }

    public void setCostPerDay(BigDecimal costPerDay) {
        this.costPerDay = costPerDay;
    }

    public BigDecimal getLateReturnFinePerDay() {
        return lateReturnFinePerDay;
    }

    public void setLateReturnFinePerDay(BigDecimal lateReturnFinePerDay) {
        this.lateReturnFinePerDay = lateReturnFinePerDay;
    }
}