package dev.burikk.carrentz.app.api.service.merchant.vehicle.response;

import dev.burikk.carrentz.app.api.item.LovItem;

import java.util.ArrayList;
import java.util.List;

public class VehicleResourceResponse {
    private List<LovItem> stores;
    private List<LovItem> vehicleTypes;

    {
        this.stores = new ArrayList<>();
        this.vehicleTypes = new ArrayList<>();
    }

    public List<LovItem> getStores() {
        return stores;
    }

    public void setStores(List<LovItem> stores) {
        this.stores = stores;
    }

    public List<LovItem> getVehicleTypes() {
        return vehicleTypes;
    }

    public void setVehicleTypes(List<LovItem> vehicleTypes) {
        this.vehicleTypes = vehicleTypes;
    }
}