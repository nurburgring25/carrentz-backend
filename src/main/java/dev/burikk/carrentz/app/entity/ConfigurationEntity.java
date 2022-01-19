package dev.burikk.carrentz.app.entity;

import dev.burikk.carrentz.engine.datasource.annotation.*;
import dev.burikk.carrentz.engine.entity.Entity;

import java.math.BigDecimal;
import java.sql.JDBCType;

/**
 * @author Muhammad Irfan
 * @since 19/01/2022 09.51
 */
@MarkTable("configurations")
@UniqueKeyConstraint(id = 1, value = "configurations_uk")
public class ConfigurationEntity extends Entity {
    @UniqueKey(1)
    @MarkColumn(
            value = "merchant_id",
            jdbcType = JDBCType.BIGINT,
            isNotNull = true,
            columnReference = "id",
            tableReference = "merchants"
    )
    private Long merchantId;

    @MarkColumn(
            value = "cost_driver_per_day",
            jdbcType = JDBCType.NUMERIC,
            isNotNull = true
    )
    private BigDecimal costDriverPerDay;

    @MarkColumn(
            value = "maximum_rent_duration",
            jdbcType = JDBCType.NUMERIC,
            isNotNull = true
    )
    private BigDecimal maximumRentDuration;

    public Long getMerchantId() {
        return this.merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public BigDecimal getCostDriverPerDay() {
        return this.costDriverPerDay;
    }

    public void setCostDriverPerDay(BigDecimal costDriverPerDay) {
        this.costDriverPerDay = costDriverPerDay;
    }

    public BigDecimal getMaximumRentDuration() {
        return this.maximumRentDuration;
    }

    public void setMaximumRentDuration(BigDecimal maximumRentDuration) {
        this.maximumRentDuration = maximumRentDuration;
    }
}