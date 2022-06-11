package dev.burikk.carrentz.app.api.service.user.vehicle.item;

import java.io.Serializable;

public class UserVehicleImageItem implements Serializable {
    private Long id;
    private boolean thumbnail;
    private String url;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(boolean thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}