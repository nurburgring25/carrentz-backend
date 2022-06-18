package dev.burikk.carrentz.app.api.service.merchant.report.response;

import dev.burikk.carrentz.app.api.service.merchant.report.item.RentByVehicleItem;

import java.util.ArrayList;
import java.util.List;

public class RentByVehicleResponse {
    private List<RentByVehicleItem> rentByVehicleItems;

    {
        this.rentByVehicleItems = new ArrayList<>();
    }

    public List<RentByVehicleItem> getRentByVehicleItems() {
        return rentByVehicleItems;
    }

    public void setRentByVehicleItems(List<RentByVehicleItem> rentByVehicleItems) {
        this.rentByVehicleItems = rentByVehicleItems;
    }
}