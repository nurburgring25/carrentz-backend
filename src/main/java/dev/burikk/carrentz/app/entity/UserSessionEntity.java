package dev.burikk.carrentz.app.entity;

import dev.burikk.carrentz.engine.common.WynixSession;
import dev.burikk.carrentz.engine.common.WynixUser;
import dev.burikk.carrentz.engine.datasource.DMLManager;
import dev.burikk.carrentz.engine.datasource.annotation.*;
import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.entity.annotation.AllowSpecialCharacter;
import dev.burikk.carrentz.engine.entity.annotation.MarkAuditable;

import javax.naming.NamingException;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * @author Muhammad Irfan
 * @since 10/01/2019 12.56
 */
@MarkAuditable
@MarkTable("user_sessions")
@PrimaryKeyConstraint("user_sessions_pk")
@UniqueKeyConstraint(id = 1, value = "user_sessions_session_id_uk")
@UniqueKeyConstraint(id = 2, value = "user_sessions_fingerprint_id_uk")
@AllowSpecialCharacter
public class UserSessionEntity extends Entity implements WynixSession {
    //<editor-fold desc="Column">
    @PrimaryKey
    @MarkColumn(
            value = "id",
            jdbcType = JDBCType.ROWID,
            isNotNull = true
    )
    private Long id;

    @UniqueKey(2)
    @MarkColumn(
            value = "user_id",
            jdbcType = JDBCType.VARCHAR,
            isNotNull = true,
            columnReference = "id",
            tableReference = "users"
    )
    private String userId;

    @UniqueKey(1)
    @MarkColumn(
            value = "session_id",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 1024,
            isNotNull = true
    )
    private String sessionId;

    @UniqueKey(2)
    @MarkColumn(
            value = "fingerprint_id",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 128,
            isNotNull = true
    )
    private String fingerprintId;

    @MarkColumn(
            value = "platform",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 64,
            isNotNull = true
    )
    private String platform;

    @MarkColumn(
            value = "version",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 16,
            isNotNull = true
    )
    private String version;

    @MarkColumn(
            value = "description",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 256,
            isNotNull = true
    )
    private String description;

    @MarkColumn(
            value = "ip_address",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 32,
            isNotNull = true
    )
    private String ipAddress;

    @MarkColumn(
            value = "location",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 128
    )
    private String location;

    @MarkColumn(
            value = "last_active",
            jdbcType = JDBCType.TIMESTAMP,
            isNotNull = true,
            defaultValue = "NOW()"
    )
    private LocalDateTime lastActive;
    //</editor-fold>

    //<editor-fold desc="Property">
    private UserEntity userEntity;
    //</editor-fold>

    @Override
    public WynixUser getWynixUser() throws SQLException, InstantiationException, IllegalAccessException, NamingException {
        if (this.userId != null && this.userEntity == null) {
            this.userEntity = DMLManager.getEntity(UserEntity.class, this.userId);
        }

        return this.userEntity;
    }

    //<editor-fold desc="Getter and setter">
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

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getFingerprintId() {
        return this.fingerprintId;
    }

    public void setFingerprintId(String fingerprintId) {
        this.fingerprintId = fingerprintId;
    }

    public String getPlatform() {
        return this.platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getLastActive() {
        return this.lastActive;
    }

    public void setLastActive(LocalDateTime lastActive) {
        this.lastActive = lastActive;
    }

    public UserEntity getUserEntity() {
        return this.userEntity;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }
    //</editor-fold>
}