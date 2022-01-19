package dev.burikk.carrentz.app.entity;

import dev.burikk.carrentz.app.common.CustomConstant;
import dev.burikk.carrentz.engine.common.SessionEntity;
import dev.burikk.carrentz.engine.datasource.annotation.MarkColumn;
import dev.burikk.carrentz.engine.datasource.annotation.MarkTable;
import dev.burikk.carrentz.engine.datasource.annotation.PrimaryKeyConstraint;

import java.sql.JDBCType;

/**
 * @author Muhammad Irfan
 * @since 19/01/2022 09.57
 */
@MarkTable("owners")
@PrimaryKeyConstraint("owners_pk")
public class OwnerEntity extends SessionEntity {
    @MarkColumn(
            value = CustomConstant.Reflection.FIELD_MERCHANT_ID,
            jdbcType = JDBCType.BIGINT,
            isNotNull = true,
            columnReference = "id",
            tableReference = "merchants"
    )
    private Long merchantId;

    public Long getMerchantId() {
        return this.merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }
}