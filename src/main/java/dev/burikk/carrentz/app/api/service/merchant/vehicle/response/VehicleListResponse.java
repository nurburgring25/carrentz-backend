package dev.burikk.carrentz.app.api.service.merchant.vehicle.response;

import dev.burikk.carrentz.app.api.service.merchant.vehicle.item.VehicleItem;

import java.util.ArrayList;
import java.util.List;

public class VehicleListResponse {
    private List<VehicleItem> details;

    {
        this.details = new ArrayList<>();
    }

    public List<VehicleItem> getDetails() {
        return details;
    }

    public void setDetails(List<VehicleItem> details) {
        this.details = details;
    }
}