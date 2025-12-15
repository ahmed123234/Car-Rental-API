package com.carrental.entity.enums;

public enum RefundStatus {
    INITIATED("Refund initiated"),
    PROCESSING("Refund processing"),
    COMPLETED("Refund completed"),
    FAILED("Refund failed"),
    REJECTED("Refund rejected");
    
    private final String description;
    
    RefundStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
