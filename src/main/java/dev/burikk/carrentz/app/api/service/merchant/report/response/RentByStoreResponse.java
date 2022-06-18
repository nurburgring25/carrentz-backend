package dev.burikk.carrentz.app.api.service.merchant.report.response;

import dev.burikk.carrentz.app.api.service.merchant.report.item.RentByStoreItem;

import java.util.ArrayList;
import java.util.List;

public class RentByStoreResponse {
    private List<RentByStoreItem> rentByStoreItems;

    {
        this.rentByStoreItems = new ArrayList<>();
    }

    public List<RentByStoreItem> getRentByStoreItems() {
        return rentByStoreItems;
    }

    public void setRentByStoreItems(List<RentByStoreItem> rentByStoreItems) {
        this.rentByStoreItems = rentByStoreItems;
    }
}