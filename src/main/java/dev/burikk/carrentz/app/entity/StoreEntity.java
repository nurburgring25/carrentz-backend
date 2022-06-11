package dev.burikk.carrentz.app.entity;

import dev.burikk.carrentz.app.common.CustomConstant;
import dev.burikk.carrentz.engine.datasource.annotation.*;
import dev.burikk.carrentz.engine.datasource.enumeration.JoinType;
import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.entity.annotation.MarkAuditable;

import java.sql.JDBCType;

/**
 * @author Muhammad Irfan
 * @since 19/01/2022 09.51
 */
@MarkAuditable
@MarkTable("stores")
@PrimaryKeyConstraint("stores_pk")
@UniqueKeyConstraint(id = 1, value = "store_name_uk")
@Reference(id = 1, sourceColumn = "merchant_id", targetTable = "merchants", joinType = JoinType.INNER_JOIN)
public class StoreEntity extends Entity {
    @PrimaryKey
    @MarkColumn(
            value = "id",
            jdbcType = JDBCType.ROWID,
            isNotNull = true
    )
    private Long id;

    @UniqueKey(1)
    @MarkColumn(
            value = CustomConstant.Reflection.FIELD_MERCHANT_ID,
            jdbcType = JDBCType.BIGINT,
            isNotNull = true,
            columnReference = "id",
            tableReference = "merchants"
    )
    private Long merchantId;

    @MarkReferencedColumn(
            value = "name",
            jdbcType = JDBCType.VARCHAR,
            referenceID = 1,
            alias = "merchant_name"
    )
    private String merchantName;

    @UniqueKey(1)
    @MarkColumn(
            value = "name",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 64,
            isNotNull = true
    )
    private String name;

    @MarkColumn(
            value = "phone_number",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 32,
            isNotNull = true
    )
    private String phoneNumber;

    @MarkColumn(
            value = "address",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 256,
            isNotNull = true
    )
    private String address;

    @MarkColumn(
            value = "city",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 128,
            isNotNull = true
    )
    private String city;

    @MarkColumn(
            value = "image",
            jdbcType = JDBCType.BINARY
    )
    private byte[] image;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMerchantId() {
        return this.merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}