package dev.burikk.carrentz.app.api.service.merchant.vehicleavailibility.item;

import java.time.LocalDate;

public class RentItem {
    private long id;
    private LocalDate date;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}