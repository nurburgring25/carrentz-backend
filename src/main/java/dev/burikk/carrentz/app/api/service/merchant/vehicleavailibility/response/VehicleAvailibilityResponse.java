package dev.burikk.carrentz.app.api.service.merchant.vehicleavailibility.response;

import dev.burikk.carrentz.app.api.service.merchant.vehicleavailibility.item.RentItem;

import java.util.ArrayList;
import java.util.List;

public class VehicleAvailibilityResponse {
    private List<RentItem> rentItems;

    {
        this.rentItems = new ArrayList<>();
    }

    public List<RentItem> getRentItems() {
        return rentItems;
    }

    public void setRentItems(List<RentItem> rentItems) {
        this.rentItems = rentItems;
    }
}