package dev.burikk.carrentz.app.api.service.user.rent.request;

import java.time.LocalDate;

/**
 * @author Muhammad Irfan
 * @since 18/02/2022 14.05
 */
public class RentRequest {
    private Long vehicleId;
    private LocalDate start;
    private LocalDate until;

    public Long getVehicleId() {
        return this.vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public LocalDate getStart() {
        return this.start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getUntil() {
        return this.until;
    }

    public void setUntil(LocalDate until) {
        this.until = until;
    }
}