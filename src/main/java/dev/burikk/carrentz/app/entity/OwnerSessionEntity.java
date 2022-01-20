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
@MarkTable("owner_sessions")
@PrimaryKeyConstraint("owner_sessions_pk")
@UniqueKeyConstraint(id = 1, value = "owner_sessions_session_id_uk")
@UniqueKeyConstraint(id = 2, value = "owner_sessions_fingerprint_id_uk")
@AllowSpecialCharacter
public class OwnerSessionEntity extends Entity implements WynixSession {
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
            value = "owner_id",
            jdbcType = JDBCType.VARCHAR,
            isNotNull = true,
            columnReference = "id",
            tableReference = "owners"
    )
    private String ownerId;

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
    private OwnerEntity ownerEntity;
    //</editor-fold>

    @Override
    public WynixUser getWynixUser() throws SQLException, InstantiationException, IllegalAccessException, NamingException {
        if (this.ownerId != null && this.ownerEntity == null) {
            this.ownerEntity = DMLManager.getEntity(OwnerEntity.class, this.ownerId);
        }

        return this.ownerEntity;
    }

    //<editor-fold desc="Getter and setter">
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
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

    public OwnerEntity getOwnerEntity() {
        return this.ownerEntity;
    }

    public void setOwnerEntity(OwnerEntity ownerEntity) {
        this.ownerEntity = ownerEntity;
    }
    //</editor-fold>
}