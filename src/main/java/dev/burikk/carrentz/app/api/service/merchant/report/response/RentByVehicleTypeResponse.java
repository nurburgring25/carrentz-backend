package dev.burikk.carrentz.app.api.service.merchant.report.response;

import dev.burikk.carrentz.app.api.service.merchant.report.item.RentByVehicleTypeItem;

import java.util.ArrayList;
import java.util.List;

public class RentByVehicleTypeResponse {
    private List<RentByVehicleTypeItem> rentByVehicleTypeItems;

    {
        this.rentByVehicleTypeItems = new ArrayList<>();
    }

    public List<RentByVehicleTypeItem> getRentByVehicleTypeItems() {
        return rentByVehicleTypeItems;
    }

    public void setRentByVehicleTypeItems(List<RentByVehicleTypeItem> rentByVehicleTypeItems) {
        this.rentByVehicleTypeItems = rentByVehicleTypeItems;
    }
}