package com.carrental.service;

import com.carrental.dto.response.RevenueReport;
import com.carrental.dto.response.VehicleReport;
import com.carrental.dto.response.UserReport;
import com.carrental.repository.PaymentRepository;
import com.carrental.repository.RentalRepository;
import com.carrental.repository.VehicleRepository;
import com.carrental.repository.UserRepository;
import com.carrental.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminReportService {
    
    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    
    /**
     * Generate revenue report for date range
     */
    public RevenueReport generateRevenueReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating revenue report from {} to {}", startDate, endDate);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        BigDecimal totalRevenue = paymentRepository.calculateTotalRevenueFrom(startDateTime)
            .orElse(BigDecimal.ZERO);
        
        long totalTransactions = paymentRepository.countCompletedPaymentsSince(startDateTime);
        BigDecimal averageTransactionValue = BigDecimal.ZERO;
        if (totalTransactions > 0 && totalRevenue.signum() > 0) {
            averageTransactionValue = totalRevenue.divide(BigDecimal.valueOf(totalTransactions), 2, java.math.RoundingMode.HALF_UP);
        }
        
        String period = calculatePeriod(startDate, endDate);
        
        return RevenueReport.builder()
            .startDate(startDate)
            .endDate(endDate)
            .period(period)
            .totalRevenue(totalRevenue)
            .totalRefunds(BigDecimal.ZERO)
            .netRevenue(totalRevenue)
            .totalTransactions(totalTransactions)
            .successfulTransactions(totalTransactions)
            .failedTransactions(0L)
            .averageTransactionValue(averageTransactionValue)
            .dailyBreakdown(new ArrayList<>())
            .build();
    }
    
    /**
     * Generate vehicle performance report
     */
    public Page<VehicleReport> generateVehicleReport(Pageable pageable) {
        log.info("Generating vehicle performance report");
        
        return vehicleRepository.findAll(pageable)
            .map(vehicle -> VehicleReport.builder()
                .vehicleId(vehicle.getId())
                .vehicleMakeModel(vehicle.getMake() + " " + vehicle.getModel())
                .licensePlate(vehicle.getLicensePlate())
                .totalRentals(rentalRepository.count())
                .status(vehicle.getStatus().name())
                .build());
    }
    
    /**
     * Generate user activity report
     */
    public Page<UserReport> generateUserReport(Pageable pageable) {
        log.info("Generating user activity report");
        
        return userRepository.findAll(pageable)
            .map(user -> UserReport.builder()
                .userId(user.getId())
                .userName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .registrationDate(user.getCreatedAt())
                .build());
    }
    
    /**
     * Get top customers by spending
     */
    public List<UserReport> getTopCustomers(int limit) {
        log.info("Fetching top {} customers by spending", limit);
        // Implementation would query users sorted by total spending
        return new ArrayList<>();
    }
    
    /**
     * Get most rented vehicles
     */
    public List<VehicleReport> getMostRentedVehicles(int limit) {
        log.info("Fetching top {} most rented vehicles", limit);
        // Implementation would query vehicles sorted by rental count
        return new ArrayList<>();
    }
    
    private String calculatePeriod(LocalDate startDate, LocalDate endDate) {
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        if (daysDiff == 0) return "Daily";
        if (daysDiff <= 7) return "Weekly";
        if (daysDiff <= 31) return "Monthly";
        return "Annual";
    }
}

