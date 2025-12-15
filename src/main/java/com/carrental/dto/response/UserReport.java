package com.carrental.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReport {
    
    private Long userId;
    private String userName;
    private String email;
    
    private Long totalRentals;
    private Long completedRentals;
    private Long cancelledRentals;
    
    private BigDecimal totalSpent;
    private BigDecimal averageSpentPerRental;
    
    private Long totalReviews;
    private Double averageRating;
    
    private Long totalPayments;
    private Long failedPayments;
    
    private LocalDateTime registrationDate;
    private LocalDateTime lastRentalDate;
    private String userStatus; // ACTIVE, INACTIVE, SUSPENDED
}