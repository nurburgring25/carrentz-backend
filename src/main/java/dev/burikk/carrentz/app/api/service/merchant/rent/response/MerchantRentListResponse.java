package dev.burikk.carrentz.app.api.service.merchant.rent.response;

import dev.burikk.carrentz.app.api.service.merchant.rent.item.MerchantRentItem;

import java.util.ArrayList;
import java.util.List;

public class MerchantRentListResponse {
    private List<MerchantRentItem> details;

    {
        this.details = new ArrayList<>();
    }

    public List<MerchantRentItem> getDetails() {
        return details;
    }

    public void setDetails(List<MerchantRentItem> details) {
        this.details = details;
    }
}