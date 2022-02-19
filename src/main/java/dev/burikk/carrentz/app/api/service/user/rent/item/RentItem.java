package dev.burikk.carrentz.app.api.service.user.rent.item;

import dev.burikk.carrentz.app.api.service.merchant.vehicle.item.VehicleItem;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author Muhammad Irfan
 * @since 18/02/2022 15.01
 */
public class RentItem {
    private Long id;
    private VehicleItem vehicleItem;
    private String status;
    private LocalDate start;
    private LocalDate until;
    private Integer duration;
    private BigDecimal costPerDay;
    private BigDecimal total;
    private BigDecimal returnAt;

}