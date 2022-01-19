package dev.burikk.carrentz.app.entity;

import dev.burikk.carrentz.engine.datasource.annotation.*;
import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.entity.annotation.MarkAuditable;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.time.LocalDate;

/**
 * @author Muhammad Irfan
 * @since 19/01/2022 09.51
 */
@MarkAuditable
@MarkTable("rents")
@PrimaryKeyConstraint("rents_pk")
public class RentEntity extends Entity {
    @PrimaryKey
    @MarkColumn(
            value = "id",
            jdbcType = JDBCType.ROWID,
            isNotNull = true
    )
    private Long id;

    @MarkColumn(
            value = "user_id",
            jdbcType = JDBCType.VARCHAR,
            isNotNull = true,
            columnReference = "id",
            tableReference = "users"
    )
    private String userId;

    @MarkColumn(
            value = "vehicle_id",
            jdbcType = JDBCType.BIGINT,
            isNotNull = true,
            columnReference = "id",
            tableReference = "vehicles"
    )
    private Long vehicleId;

    @MarkColumn(
            value = "status",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 16,
            isNotNull = true
    )
    private String status;

    @MarkColumn(
            value = "start",
            jdbcType = JDBCType.DATE,
            isNotNull = true
    )
    private LocalDate start;

    @MarkColumn(
            value = "until",
            jdbcType = JDBCType.DATE,
            isNotNull = true
    )
    private LocalDate until;

    @MarkColumn(
            value = "duration",
            jdbcType = JDBCType.INTEGER,
            isNotNull = true
    )
    private Integer duration;

    @MarkColumn(
            value = "cost_per_day",
            jdbcType = JDBCType.NUMERIC,
            isNotNull = true
    )
    private BigDecimal costPerDay;

    @MarkColumn(
            value = "total",
            jdbcType = JDBCType.NUMERIC,
            isNotNull = true
    )
    private BigDecimal total;

    @MarkColumn(
            value = "return_at",
            jdbcType = JDBCType.DATE,
            isNotNull = true
    )
    private LocalDate returnAt;

    @MarkColumn(
            value = "late_return_duration",
            jdbcType = JDBCType.INTEGER,
            isNotNull = true
    )
    private Integer late_return_duration;

    @MarkColumn(
            value = "late_return_fine_per_day",
            jdbcType = JDBCType.NUMERIC,
            isNotNull = true
    )
    private BigDecimal lateReturnFinePerDay;

    @MarkColumn(
            value = "late_return_fine",
            jdbcType = JDBCType.NUMERIC,
            isNotNull = true
    )
    private BigDecimal lateReturnFine;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getVehicleId() {
        return this.vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getStart() {
        return this.start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getUntil() {
        return this.until;
    }

    public void setUntil(LocalDate until) {
        this.until = until;
    }

    public Integer getDuration() {
        return this.duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public BigDecimal getCostPerDay() {
        return this.costPerDay;
    }

    public void setCostPerDay(BigDecimal costPerDay) {
        this.costPerDay = costPerDay;
    }

    public BigDecimal getTotal() {
        return this.total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDate getReturnAt() {
        return this.returnAt;
    }

    public void setReturnAt(LocalDate returnAt) {
        this.returnAt = returnAt;
    }

    public Integer getLate_return_duration() {
        return this.late_return_duration;
    }

    public void setLate_return_duration(Integer late_return_duration) {
        this.late_return_duration = late_return_duration;
    }

    public BigDecimal getLateReturnFinePerDay() {
        return this.lateReturnFinePerDay;
    }

    public void setLateReturnFinePerDay(BigDecimal lateReturnFinePerDay) {
        this.lateReturnFinePerDay = lateReturnFinePerDay;
    }

    public BigDecimal getLateReturnFine() {
        return this.lateReturnFine;
    }

    public void setLateReturnFine(BigDecimal lateReturnFine) {
        this.lateReturnFine = lateReturnFine;
    }
}