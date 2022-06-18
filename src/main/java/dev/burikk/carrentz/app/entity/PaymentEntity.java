package dev.burikk.carrentz.app.entity;

import dev.burikk.carrentz.engine.datasource.annotation.MarkColumn;
import dev.burikk.carrentz.engine.datasource.annotation.MarkTable;
import dev.burikk.carrentz.engine.datasource.annotation.PrimaryKey;
import dev.burikk.carrentz.engine.datasource.annotation.PrimaryKeyConstraint;
import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.entity.annotation.MarkAuditable;

import java.math.BigDecimal;
import java.sql.JDBCType;

/**
 * @author Muhammad Irfan
 * @since 19/01/2022 09.51
 */
@MarkAuditable
@MarkTable("payments")
@PrimaryKeyConstraint("payments_pk")
public class PaymentEntity extends Entity {
    @PrimaryKey
    @MarkColumn(
            value = "id",
            jdbcType = JDBCType.ROWID,
            isNotNull = true
    )
    private Long id;

    @MarkColumn(
            value = "rent_id",
            jdbcType = JDBCType.BIGINT,
            isNotNull = true,
            columnReference = "id",
            tableReference = "rents"
    )
    private Long rentId;

    @MarkColumn(
            value = "type",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 16,
            isNotNull = true
    )
    private String type;

    @MarkColumn(
            value = "amount",
            jdbcType = JDBCType.NUMERIC,
            isNotNull = true
    )
    private BigDecimal amount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRentId() {
        return rentId;
    }

    public void setRentId(Long rentId) {
        this.rentId = rentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}