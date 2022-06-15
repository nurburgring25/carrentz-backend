package dev.burikk.carrentz.app.api.service.merchant.vehicleavailibility.response;

import dev.burikk.carrentz.app.api.service.merchant.vehicleavailibility.item.StoreItem;
import dev.burikk.carrentz.app.api.service.merchant.vehicleavailibility.item.VehicleItem;

import java.util.ArrayList;
import java.util.List;

public class VehicleAvailibilityResourceResponse {
    private List<StoreItem> storeItems;
    private List<VehicleItem> vehicleItems;

    {
        this.storeItems = new ArrayList<>();
        this.vehicleItems = new ArrayList<>();
    }

    public List<StoreItem> getStoreItems() {
        return storeItems;
    }

    public void setStoreItems(List<StoreItem> storeItems) {
        this.storeItems = storeItems;
    }

    public List<VehicleItem> getVehicleItems() {
        return vehicleItems;
    }

    public void setVehicleItems(List<VehicleItem> vehicleItems) {
        this.vehicleItems = vehicleItems;
    }
}