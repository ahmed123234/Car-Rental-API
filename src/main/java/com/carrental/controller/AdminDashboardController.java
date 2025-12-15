package com.carrental.controller;

import com.carrental.dto.response.DashboardMetrics;
import com.carrental.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Dashboard", description = "Admin dashboard metrics and KPIs")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {
    
    private final AdminDashboardService adminDashboardService;
    
    @GetMapping("/metrics")
    @Operation(summary = "Get comprehensive dashboard metrics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard metrics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Admin role required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<DashboardMetrics> getDashboardMetrics() {
        log.info("Fetching comprehensive dashboard metrics");
        
        DashboardMetrics metrics = adminDashboardService.getDashboardMetrics();
        
        log.debug("Dashboard metrics retrieved: {} rentals, {} revenue", 
            metrics.getTotalRentals(), metrics.getTotalRevenue());
        
        return ResponseEntity.ok(metrics);
    }
    
    @GetMapping("/revenue-trend")
    @Operation(summary = "Get revenue trend for last N days")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Revenue trend retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid number of days"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<?>> getRevenueTrend(
        @Parameter(description = "Number of days to look back (1-365)", example = "30")
        @RequestParam(defaultValue = "30") int days) {
        
        log.info("Fetching revenue trend for last {} days", days);
        
        if (days < 1 || days > 365) {
            log.warn("Invalid days parameter: {}", days);
            return ResponseEntity.badRequest().build();
        }
        
        var trends = adminDashboardService.getRevenueByDay(days);
        
        log.debug("Revenue trend retrieved with {} data points", trends.size());
        
        return ResponseEntity.ok(trends);
    }
    
    @GetMapping("/fleet-utilization")
    @Operation(summary = "Get fleet utilization metrics")
    public ResponseEntity<?> getFleetUtilization() {
        log.info("Fetching fleet utilization metrics");
        
        DashboardMetrics metrics = adminDashboardService.getDashboardMetrics();
        
        return ResponseEntity.ok(new Object() {
            public final Long totalVehicles = metrics.getTotalVehicles();
            public final Long availableVehicles = metrics.getAvailableVehicles();
            public final Long maintenanceVehicles = metrics.getMaintenanceVehicles();
            public final Double utilizationRate = metrics.getFleetUtilizationRate();
        });
    }
    
    @GetMapping("/payment-metrics")
    @Operation(summary = "Get payment-related metrics")
    public ResponseEntity<?> getPaymentMetrics() {
        log.info("Fetching payment metrics");
        
        DashboardMetrics metrics = adminDashboardService.getDashboardMetrics();
        
        return ResponseEntity.ok(new Object() {
            public final Long totalPayments = metrics.getTotalPayments();
            public final Long completedPayments = metrics.getCompletedPayments();
            public final Long pendingPayments = metrics.getPendingPayments();
            public final Long failedPayments = metrics.getFailedPayments();
            public final java.math.BigDecimal totalRevenue = metrics.getTotalRevenue();
            public final java.math.BigDecimal dailyRevenue = metrics.getDailyRevenue();
        });
    }
    
    @GetMapping("/user-metrics")
    @Operation(summary = "Get user-related metrics")
    public ResponseEntity<?> getUserMetrics() {
        log.info("Fetching user metrics");
        
        DashboardMetrics metrics = adminDashboardService.getDashboardMetrics();
        
        return ResponseEntity.ok(new Object() {
            public final Long totalUsers = metrics.getTotalUsers();
            public final Long activeUsers = metrics.getActiveUsers();
            public final Long newUsersThisMonth = metrics.getNewUsersThisMonth();
        });
    }
    
    @GetMapping("/review-metrics")
    @Operation(summary = "Get review and rating metrics")
    public ResponseEntity<?> getReviewMetrics() {
        log.info("Fetching review metrics");
        
        DashboardMetrics metrics = adminDashboardService.getDashboardMetrics();
        
        return ResponseEntity.ok(new Object() {
            public final Long totalReviews = metrics.getTotalReviews();
            public final Long pendingReviews = metrics.getPendingReviews();
            public final Double averageRating = metrics.getAverageRating();
        });
    }
}