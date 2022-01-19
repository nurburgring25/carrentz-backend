package dev.burikk.carrentz.app.entity;

import dev.burikk.carrentz.engine.common.SessionEntity;
import dev.burikk.carrentz.engine.datasource.annotation.MarkColumn;
import dev.burikk.carrentz.engine.datasource.annotation.MarkTable;
import dev.burikk.carrentz.engine.datasource.annotation.PrimaryKeyConstraint;

import java.sql.JDBCType;

/**
 * @author Muhammad Irfan
 * @since 19/01/2022 09.57
 */
@MarkTable("users")
@PrimaryKeyConstraint("users_pk")
public class UserEntity extends SessionEntity {
    @MarkColumn(
            value = "phone_number",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 32,
            isNotNull = true
    )
    private String phoneNumber;

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}