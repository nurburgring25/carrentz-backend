package dev.burikk.carrentz.app.api.service.user.vehicle.item;

import java.util.ArrayList;
import java.util.List;

public class UserVehicleItem {
    private Long id;
    private Long storeId;
    private Long vehicleTypeId;
    private String storeName;
    private String vehicleTypeName;
    private String licenseNumber;
    private String name;
    private String description;
    private long costPerDay;
    private long lateReturnFinePerDay;
    private List<UserVehicleImageItem> images;

    {
        this.images = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Long getVehicleTypeId() {
        return vehicleTypeId;
    }

    public void setVehicleTypeId(Long vehicleTypeId) {
        this.vehicleTypeId = vehicleTypeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getVehicleTypeName() {
        return vehicleTypeName;
    }

    public void setVehicleTypeName(String vehicleTypeName) {
        this.vehicleTypeName = vehicleTypeName;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCostPerDay() {
        return costPerDay;
    }

    public void setCostPerDay(long costPerDay) {
        this.costPerDay = costPerDay;
    }

    public long getLateReturnFinePerDay() {
        return lateReturnFinePerDay;
    }

    public void setLateReturnFinePerDay(long lateReturnFinePerDay) {
        this.lateReturnFinePerDay = lateReturnFinePerDay;
    }

    public List<UserVehicleImageItem> getImages() {
        return images;
    }

    public void setImages(List<UserVehicleImageItem> images) {
        this.images = images;
    }
}