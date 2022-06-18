package dev.burikk.carrentz.app.api.service.merchant.report.response;

import dev.burikk.carrentz.app.api.service.merchant.report.item.DailyRentItem;

import java.util.ArrayList;
import java.util.List;

public class DailyRentResponse {
    private List<DailyRentItem> dailyRentItems;

    {
        this.dailyRentItems = new ArrayList<>();
    }

    public List<DailyRentItem> getDailyRentItems() {
        return dailyRentItems;
    }

    public void setDailyRentItems(List<DailyRentItem> dailyRentItems) {
        this.dailyRentItems = dailyRentItems;
    }
}