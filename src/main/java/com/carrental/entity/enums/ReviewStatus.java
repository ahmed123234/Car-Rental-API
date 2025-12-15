package com.carrental.entity.enums;

public enum ReviewStatus {
    PENDING("Pending approval"),
    APPROVED("Approved and visible"),
    REJECTED("Rejected and hidden"),
    FLAGGED("Flagged for review"),
    DELETED("Deleted by user");
    
    private final String description;
    
    ReviewStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}