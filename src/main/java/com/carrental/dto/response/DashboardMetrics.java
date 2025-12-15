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
public class DashboardMetrics {
    
    private Long totalRentals;
    private Long activeRentals;
    private Long completedRentals;
    private Long cancelledRentals;
    
    private BigDecimal totalRevenue;
    private BigDecimal dailyRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal averageOrderValue;
    
    private Long totalVehicles;
    private Long availableVehicles;
    private Long maintenanceVehicles;
    private Double fleetUtilizationRate;
    
    private Long totalUsers;
    private Long activeUsers;
    private Long newUsersThisMonth;
    
    private Long totalPayments;
    private Long completedPayments;
    private Long pendingPayments;
    private Long failedPayments;
    
    private Long totalReviews;
    private Long pendingReviews;
    private Double averageRating;
    
    private Long totalRefunds;
    private BigDecimal totalRefundAmount;
    
    private LocalDateTime lastUpdated;
}
