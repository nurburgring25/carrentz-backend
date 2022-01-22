package dev.burikk.carrentz.app.api.service.merchant.store.response;

import dev.burikk.carrentz.app.api.service.merchant.store.item.StoreItem;

import java.util.ArrayList;
import java.util.List;

public class StoreListResponse {
    private List<StoreItem> details;

    {
        this.details = new ArrayList<>();
    }

    public List<StoreItem> getDetails() {
        return details;
    }

    public void setDetails(List<StoreItem> details) {
        this.details = details;
    }
}