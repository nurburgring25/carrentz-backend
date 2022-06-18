package dev.burikk.carrentz.app.api.service.merchant.report.item;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailyRentItem {
    private LocalDate date;
    private BigDecimal downPayment;
    private BigDecimal amount;
    private BigDecimal lateReturnFine;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getDownPayment() {
        return downPayment;
    }

    public void setDownPayment(BigDecimal downPayment) {
        this.downPayment = downPayment;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getLateReturnFine() {
        return lateReturnFine;
    }

    public void setLateReturnFine(BigDecimal lateReturnFine) {
        this.lateReturnFine = lateReturnFine;
    }
}