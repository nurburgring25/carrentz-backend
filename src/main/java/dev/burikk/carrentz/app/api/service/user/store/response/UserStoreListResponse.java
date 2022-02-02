package dev.burikk.carrentz.app.api.service.user.store.response;

import dev.burikk.carrentz.app.api.service.user.store.item.UserStoreItem;

import java.util.ArrayList;
import java.util.List;

public class UserStoreListResponse {
    private List<UserStoreItem> details;

    {
        this.details = new ArrayList<>();
    }

    public List<UserStoreItem> getDetails() {
        return details;
    }

    public void setDetails(List<UserStoreItem> details) {
        this.details = details;
    }
}