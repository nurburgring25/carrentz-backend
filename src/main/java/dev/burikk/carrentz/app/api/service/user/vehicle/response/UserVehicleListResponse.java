package dev.burikk.carrentz.app.api.service.user.vehicle.response;

import dev.burikk.carrentz.app.api.service.user.vehicle.item.UserVehicleItem;

import java.util.ArrayList;
import java.util.List;

public class UserVehicleListResponse {
    private List<UserVehicleItem> details;

    {
        this.details = new ArrayList<>();
    }

    public List<UserVehicleItem> getDetails() {
        return details;
    }

    public void setDetails(List<UserVehicleItem> details) {
        this.details = details;
    }
}