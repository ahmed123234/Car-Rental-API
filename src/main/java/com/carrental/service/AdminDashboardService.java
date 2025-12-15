package com.carrental.service;

import com.carrental.dto.response.DashboardMetrics;
import com.carrental.entity.enums.PaymentStatus;
import com.carrental.entity.enums.RentalStatus;
import com.carrental.entity.enums.ReviewStatus;
import com.carrental.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminDashboardService {
    
    private final RentalRepository rentalRepository;
    private final PaymentRepository paymentRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final RefundRepository refundRepository;
    
    /**
     * Get comprehensive dashboard metrics
     */
    public DashboardMetrics getDashboardMetrics() {
        log.debug("Fetching dashboard metrics");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        // Rental metrics
        long totalRentals = rentalRepository.count();
        long activeRentals = 0; // Calculate based on ACTIVE status
        long completedRentals = 0; // Calculate based on COMPLETED status
        long cancelledRentals = 0; // Calculate based on CANCELLED status
        
        // Revenue metrics
        BigDecimal totalRevenue = paymentRepository.calculateTotalRevenueFrom(startOfMonth)
            .orElse(BigDecimal.ZERO);
        BigDecimal dailyRevenue = paymentRepository.calculateTotalRevenueFrom(startOfDay)
            .orElse(BigDecimal.ZERO);
        BigDecimal monthlyRevenue = totalRevenue;
        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (totalRentals > 0 && totalRevenue.signum() > 0) {
            averageOrderValue = totalRevenue.divide(BigDecimal.valueOf(totalRentals), 2, java.math.RoundingMode.HALF_UP);
        }
        
        // Vehicle metrics
        long totalVehicles = vehicleRepository.count();
        long availableVehicles = 0; // Calculate based on ACTIVE status
        long maintenanceVehicles = 0; // Calculate based on MAINTENANCE status
        Double fleetUtilizationRate = calculateFleetUtilization();
        
        // User metrics
        long totalUsers = userRepository.count();
        long activeUsers = 0; // Calculate based on last activity
        long newUsersThisMonth = 0; // Calculate based on registration date
        
        // Payment metrics
        long totalPayments = paymentRepository.count();
        long completedPayments = 0; // Calculate based on COMPLETED status
        long pendingPayments = 0; // Calculate based on PENDING status
        long failedPayments = 0; // Calculate based on FAILED status
        
        // Review metrics
        long totalReviews = reviewRepository.count();
        long pendingReviews = 0; // Calculate based on PENDING status
        Double averageRating = reviewRepository.findAverageRatingByVehicle(null)
            .orElse(0.0);
        
        // Refund metrics
        long totalRefunds = refundRepository.count();
        BigDecimal totalRefundAmount = BigDecimal.ZERO; // Calculate based on refunds
        
        return DashboardMetrics.builder()
            .totalRentals(totalRentals)
            .activeRentals(activeRentals)
            .completedRentals(completedRentals)
            .cancelledRentals(cancelledRentals)
            .totalRevenue(totalRevenue)
            .dailyRevenue(dailyRevenue)
            .monthlyRevenue(monthlyRevenue)
            .averageOrderValue(averageOrderValue)
            .totalVehicles(totalVehicles)
            .availableVehicles(availableVehicles)
            .maintenanceVehicles(maintenanceVehicles)
            .fleetUtilizationRate(fleetUtilizationRate)
            .totalUsers(totalUsers)
            .activeUsers(activeUsers)
            .newUsersThisMonth(newUsersThisMonth)
            .totalPayments(totalPayments)
            .completedPayments(completedPayments)
            .pendingPayments(pendingPayments)
            .failedPayments(failedPayments)
            .totalReviews(totalReviews)
            .pendingReviews(pendingReviews)
            .averageRating(averageRating)
            .totalRefunds(totalRefunds)
            .totalRefundAmount(totalRefundAmount)
            .lastUpdated(LocalDateTime.now())
            .build();
    }
    
    /**
     * Calculate fleet utilization rate
     */
    private Double calculateFleetUtilization() {
        long totalVehicles = vehicleRepository.count();
        if (totalVehicles == 0) {
            return 0.0;
        }
        
        long activeRentals = rentalRepository.count();
        return (activeRentals * 100.0) / totalVehicles;
    }
    
    /**
     * Get revenue trend data
     */
    public java.util.List<Object[]> getRevenueByDay(int days) {
        log.debug("Fetching revenue trend for last {} days", days);
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return paymentRepository.calculateTotalRevenueFrom(startDate)
            .map(rev -> new Object[]{startDate, rev})
            .stream()
            .toList();
    }
}

