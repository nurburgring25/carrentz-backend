package dev.burikk.carrentz.engine.common;

/**
 * @author Muhammad Irfan
 * @since 19/04/2019 18.57
 */
public class RequestMeta {
    private String fingerprintId;
    private String platform;
    private String version;
    private String description;
    private String ipAddress;
    private String location;

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
}