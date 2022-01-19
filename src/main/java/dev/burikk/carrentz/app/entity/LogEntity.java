package dev.burikk.carrentz.app.entity;

import dev.burikk.carrentz.engine.datasource.annotation.MarkColumn;
import dev.burikk.carrentz.engine.datasource.annotation.MarkTable;
import dev.burikk.carrentz.engine.datasource.annotation.PrimaryKey;
import dev.burikk.carrentz.engine.datasource.annotation.PrimaryKeyConstraint;
import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.entity.annotation.AllowSpecialCharacter;

import java.sql.JDBCType;
import java.time.LocalDateTime;

/**
 * @author Muhammad Irfan
 * @since 04/03/2019 16.14
 */
@MarkTable("logs")
@PrimaryKeyConstraint("logs_pk")
@AllowSpecialCharacter
public class LogEntity extends Entity {
    //<editor-fold desc="Column">
    @PrimaryKey
    @MarkColumn(
            value = "id",
            jdbcType = JDBCType.ROWID,
            isNotNull = true
    )
    private Long id;

    @MarkColumn(
            value = "fingerprint_id",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 128,
            isNotNull = true
    )
    private String fingerprintId;

    @MarkColumn(
            value = "request_at",
            jdbcType = JDBCType.TIMESTAMP,
            isNotNull = true
    )
    private LocalDateTime requestAt;

    @MarkColumn(
            value = "request_header",
            jdbcType = JDBCType.LONGVARCHAR
    )
    private String requestHeader;

    @MarkColumn(
            value = "request_body",
            jdbcType = JDBCType.LONGVARCHAR
    )
    private String requestBody;

    @MarkColumn(
            value = "response_at",
            jdbcType = JDBCType.TIMESTAMP
    )
    private LocalDateTime responseAt;

    @MarkColumn(
            value = "response_header",
            jdbcType = JDBCType.LONGVARCHAR
    )
    private String responseHeader;

    @MarkColumn(
            value = "response_body",
            jdbcType = JDBCType.LONGVARCHAR
    )
    private String responseBody;

    @MarkColumn(
            value = "response_status",
            jdbcType = JDBCType.INTEGER
    )
    private Integer responseStatus;

    @MarkColumn(
            value = "resource_path",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 256,
            isNotNull = true
    )
    private String resourcePath;

    @MarkColumn(
            value = "http_method",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 16,
            isNotNull = true
    )
    private String httpMethod;

    @MarkColumn(
            value = "time_elapsed",
            jdbcType = JDBCType.BIGINT,
            isNotNull = true,
            defaultValue = "0"
    )
    private Long timeElapsed;

    @MarkColumn(
            value = "exception_stack_trace",
            jdbcType = JDBCType.LONGVARCHAR
    )
    private String exceptionStackTrace;

    @MarkColumn(
            value = "platform",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 64
    )
    private String platform;

    @MarkColumn(
            value = "version",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 16
    )
    private String version;

    @MarkColumn(
            value = "description",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 256
    )
    private String description;

    @MarkColumn(
            value = "ip_address",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 32
    )
    private String ipAddress;

    @MarkColumn(
            value = "location",
            jdbcType = JDBCType.VARCHAR,
            maxLength = 128
    )
    private String location;
    //</editor-fold>

    //<editor-fold desc="Getter and setter">
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFingerprintId() {
        return this.fingerprintId;
    }

    public void setFingerprintId(String fingerprintId) {
        this.fingerprintId = fingerprintId;
    }

    public LocalDateTime getRequestAt() {
        return this.requestAt;
    }

    public void setRequestAt(LocalDateTime requestAt) {
        this.requestAt = requestAt;
    }

    public String getRequestHeader() {
        return this.requestHeader;
    }

    public void setRequestHeader(String requestHeader) {
        this.requestHeader = requestHeader;
    }

    public String getRequestBody() {
        return this.requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public LocalDateTime getResponseAt() {
        return this.responseAt;
    }

    public void setResponseAt(LocalDateTime responseAt) {
        this.responseAt = responseAt;
    }

    public String getResponseHeader() {
        return this.responseHeader;
    }

    public void setResponseHeader(String responseHeader) {
        this.responseHeader = responseHeader;
    }

    public String getResponseBody() {
        return this.responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public Integer getResponseStatus() {
        return this.responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResourcePath() {
        return this.resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getHttpMethod() {
        return this.httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Long getTimeElapsed() {
        return this.timeElapsed;
    }

    public void setTimeElapsed(Long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public String getExceptionStackTrace() {
        return this.exceptionStackTrace;
    }

    public void setExceptionStackTrace(String exceptionStackTrace) {
        this.exceptionStackTrace = exceptionStackTrace;
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
    //</editor-fold>
}