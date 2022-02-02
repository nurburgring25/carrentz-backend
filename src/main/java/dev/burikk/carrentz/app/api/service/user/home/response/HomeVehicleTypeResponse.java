package dev.burikk.carrentz.app.api.service.user.home.response;

import dev.burikk.carrentz.app.api.service.user.home.item.HomeVehicleTypeItem;

import java.util.ArrayList;
import java.util.List;

public class HomeVehicleTypeResponse {
    private List<HomeVehicleTypeItem> details;

    {
        this.details = new ArrayList<>();
    }

    public List<HomeVehicleTypeItem> getDetails() {
        return details;
    }

    public void setDetails(List<HomeVehicleTypeItem> details) {
        this.details = details;
    }
}