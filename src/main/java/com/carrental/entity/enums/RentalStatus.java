package com.carrental.entity.enums;

public enum RentalStatus {
    PENDING("Pending confirmation"),
    CONFIRMED("Confirmed by admin"),
    ACTIVE("Currently renting"),
    COMPLETED("Rental completed"),
    CANCELLED("Booking cancelled");
    
    private final String description;
    
    RentalStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
