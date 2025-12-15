package com.carrental.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateConfigurationRequest {
    
    @Min(value = 1, message = "Full refund days must be at least 1")
    private Integer fullRefundDays;
    
    @Min(value = 1, message = "Partial refund threshold must be at least 1")
    private Integer partialRefundThresholdDays;
    
    @Min(value = 0, message = "Refund percentage must be >= 0")
    @Max(value = 100, message = "Refund percentage must be <= 100")
    private Integer partialRefundPercentage;
    
    @DecimalMin(value = "0.0", message = "Tax rate must be >= 0")
    @DecimalMax(value = "1.0", message = "Tax rate must be <= 1")
    private BigDecimal taxRate;
    
    @DecimalMin(value = "0.0", message = "Late fee per hour must be >= 0")
    private BigDecimal lateFeePerHour;
    
    @Min(value = 1, message = "Review deadline must be at least 1 day")
    private Integer reviewSubmissionDeadlineDays;
    
    @NotBlank(message = "Support email is required")
    @Email(message = "Support email must be valid")
    private String supportEmail;
    
    private Boolean maintenanceMode;
    private Boolean profanityCheckEnabled;
    private Boolean moderationRequired;
}
