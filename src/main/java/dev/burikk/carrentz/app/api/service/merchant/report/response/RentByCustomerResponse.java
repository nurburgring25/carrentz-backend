package dev.burikk.carrentz.app.api.service.merchant.report.response;

import dev.burikk.carrentz.app.api.service.merchant.report.item.RentByCustomerItem;

import java.util.ArrayList;
import java.util.List;

public class RentByCustomerResponse {
    private List<RentByCustomerItem> rentByCustomerItems;

    {
        this.rentByCustomerItems = new ArrayList<>();
    }

    public List<RentByCustomerItem> getRentByCustomerItems() {
        return rentByCustomerItems;
    }

    public void setRentByCustomerItems(List<RentByCustomerItem> rentByCustomerItems) {
        this.rentByCustomerItems = rentByCustomerItems;
    }
}