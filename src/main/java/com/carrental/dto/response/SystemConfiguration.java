package com.carrental.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfiguration {
    
    // Cancellation Policy
    private Integer fullRefundDays;
    private Integer partialRefundThresholdDays;
    private Integer partialRefundPercentage;
    private Integer minimalRefundThresholdHours;
    
    // Late Fees
    private Boolean lateFeeEnabled;
    private BigDecimal lateFeePerHour;
    private BigDecimal lateFeePerDay;
    
    // Payment Configuration
    private BigDecimal taxRate;
    private String currencySymbol;
    private String invoicePrefix;
    
    // Review Configuration
    private Integer reviewSubmissionDeadlineDays;
    private Integer maxReviewTitleLength;
    private Integer maxReviewContentLength;
    private Boolean profanityCheckEnabled;
    private Boolean moderationRequired;
    
    // System Settings
    private String applicationName;
    private String applicationVersion;
    private String supportEmail;
    private Boolean maintenanceMode;
}
