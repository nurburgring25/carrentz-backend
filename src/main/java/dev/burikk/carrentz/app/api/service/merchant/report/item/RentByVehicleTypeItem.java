package dev.burikk.carrentz.app.api.service.merchant.report.item;

import java.math.BigDecimal;

public class RentByVehicleTypeItem {
    private String name;
    private long duration;
    private BigDecimal amount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}