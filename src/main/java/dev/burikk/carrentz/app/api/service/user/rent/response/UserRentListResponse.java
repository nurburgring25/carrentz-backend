package dev.burikk.carrentz.app.api.service.user.rent.response;

import dev.burikk.carrentz.app.api.service.user.rent.item.UserRentItem;

import java.util.ArrayList;
import java.util.List;

public class UserRentListResponse {
    private List<UserRentItem> details;

    {
        this.details = new ArrayList<>();
    }

    public List<UserRentItem> getDetails() {
        return details;
    }

    public void setDetails(List<UserRentItem> details) {
        this.details = details;
    }
}