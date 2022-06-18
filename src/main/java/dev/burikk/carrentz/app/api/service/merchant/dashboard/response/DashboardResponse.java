package dev.burikk.carrentz.app.api.service.merchant.dashboard.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;

public class DashboardResponse {
    private BigDecimal incomingPayment;
    private BigDecimal incomingPaymentDifferenceAmount;
    private BigDecimal incomingPaymentDifferencePercentage;
    private LinkedHashMap<LocalDate, BigDecimal> last7DaysIncomingPayment;

    private String mostFavoriteVehicle;
    private long mostFavoriteVehicleValue;

    private String mostFavoriteVehicleType;
    private long mostFavoriteVehicleTypeValue;

    private String mostFavoriteStore;
    private BigDecimal mostFavoriteStoreValue;

    private String mostFavoriteCustomer;
    private BigDecimal mostFavoriteCustomerValue;

    {
        this.last7DaysIncomingPayment = new LinkedHashMap<>();
        this.incomingPayment = BigDecimal.ZERO;
        this.incomingPaymentDifferenceAmount = BigDecimal.ZERO;
        this.incomingPaymentDifferencePercentage = BigDecimal.ZERO;
        this.mostFavoriteStoreValue = BigDecimal.ZERO;
        this.mostFavoriteCustomerValue = BigDecimal.ZERO;
    }

    public BigDecimal getIncomingPayment() {
        return incomingPayment;
    }

    public void setIncomingPayment(BigDecimal incomingPayment) {
        this.incomingPayment = incomingPayment;
    }

    public BigDecimal getIncomingPaymentDifferenceAmount() {
        return incomingPaymentDifferenceAmount;
    }

    public void setIncomingPaymentDifferenceAmount(BigDecimal incomingPaymentDifferenceAmount) {
        this.incomingPaymentDifferenceAmount = incomingPaymentDifferenceAmount;
    }

    public BigDecimal getIncomingPaymentDifferencePercentage() {
        return incomingPaymentDifferencePercentage;
    }

    public void setIncomingPaymentDifferencePercentage(BigDecimal incomingPaymentDifferencePercentage) {
        this.incomingPaymentDifferencePercentage = incomingPaymentDifferencePercentage;
    }

    public LinkedHashMap<LocalDate, BigDecimal> getLast7DaysIncomingPayment() {
        return last7DaysIncomingPayment;
    }

    public void setLast7DaysIncomingPayment(LinkedHashMap<LocalDate, BigDecimal> last7DaysIncomingPayment) {
        this.last7DaysIncomingPayment = last7DaysIncomingPayment;
    }

    public String getMostFavoriteVehicle() {
        return mostFavoriteVehicle;
    }

    public void setMostFavoriteVehicle(String mostFavoriteVehicle) {
        this.mostFavoriteVehicle = mostFavoriteVehicle;
    }

    public long getMostFavoriteVehicleValue() {
        return mostFavoriteVehicleValue;
    }

    public void setMostFavoriteVehicleValue(long mostFavoriteVehicleValue) {
        this.mostFavoriteVehicleValue = mostFavoriteVehicleValue;
    }

    public String getMostFavoriteVehicleType() {
        return mostFavoriteVehicleType;
    }

    public void setMostFavoriteVehicleType(String mostFavoriteVehicleType) {
        this.mostFavoriteVehicleType = mostFavoriteVehicleType;
    }

    public long getMostFavoriteVehicleTypeValue() {
        return mostFavoriteVehicleTypeValue;
    }

    public void setMostFavoriteVehicleTypeValue(long mostFavoriteVehicleTypeValue) {
        this.mostFavoriteVehicleTypeValue = mostFavoriteVehicleTypeValue;
    }

    public String getMostFavoriteStore() {
        return mostFavoriteStore;
    }

    public void setMostFavoriteStore(String mostFavoriteStore) {
        this.mostFavoriteStore = mostFavoriteStore;
    }

    public BigDecimal getMostFavoriteStoreValue() {
        return mostFavoriteStoreValue;
    }

    public void setMostFavoriteStoreValue(BigDecimal mostFavoriteStoreValue) {
        this.mostFavoriteStoreValue = mostFavoriteStoreValue;
    }

    public String getMostFavoriteCustomer() {
        return mostFavoriteCustomer;
    }

    public void setMostFavoriteCustomer(String mostFavoriteCustomer) {
        this.mostFavoriteCustomer = mostFavoriteCustomer;
    }

    public BigDecimal getMostFavoriteCustomerValue() {
        return mostFavoriteCustomerValue;
    }

    public void setMostFavoriteCustomerValue(BigDecimal mostFavoriteCustomerValue) {
        this.mostFavoriteCustomerValue = mostFavoriteCustomerValue;
    }
}