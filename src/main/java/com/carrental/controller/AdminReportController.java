package com.carrental.controller;

import com.carrental.dto.response.RevenueReport;
import com.carrental.dto.response.VehicleReport;
import com.carrental.dto.response.UserReport;
import com.carrental.service.AdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Reports", description = "Generate and retrieve various admin reports")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {
    
    private final AdminReportService adminReportService;
    
    @GetMapping("/revenue")
    @Operation(summary = "Generate revenue report for a date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Revenue report generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<RevenueReport> generateRevenueReport(
        @Parameter(description = "Report start date (YYYY-MM-DD)", example = "2025-01-01")
        @RequestParam 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
        LocalDate startDate,
        
        @Parameter(description = "Report end date (YYYY-MM-DD)", example = "2025-12-31")
        @RequestParam 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
        LocalDate endDate) {
        
        log.info("Generating revenue report from {} to {}", startDate, endDate);
        
        if (endDate.isBefore(startDate)) {
            log.warn("Invalid date range: end date before start date");
            return ResponseEntity.badRequest().build();
        }
        
        RevenueReport report = adminReportService.generateRevenueReport(startDate, endDate);
        
        log.info("Revenue report generated successfully with total revenue: {}", report.getTotalRevenue());
        
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/vehicles")
    @Operation(summary = "Generate vehicle performance report")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vehicle report generated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<VehicleReport>> generateVehicleReport(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "10") int size,
        
        @Parameter(description = "Sort field")
        @RequestParam(defaultValue = "vehicleId") String sortBy) {
        
        log.info("Generating vehicle report - page: {}, size: {}", page, size);
        
        if (page < 0 || size <= 0 || size > 100) {
            log.warn("Invalid pagination parameters: page={}, size={}", page, size);
            return ResponseEntity.badRequest().build();
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<VehicleReport> report = adminReportService.generateVehicleReport(pageable);
        
        log.info("Vehicle report generated with {} vehicles", report.getTotalElements());
        
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/users")
    @Operation(summary = "Generate user activity report")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User report generated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<UserReport>> generateUserReport(
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "10") int size,
        
        @Parameter(description = "Sort field")
        @RequestParam(defaultValue = "userId") String sortBy) {
        
        log.info("Generating user report - page: {}, size: {}", page, size);
        
        if (page < 0 || size <= 0 || size > 100) {
            log.warn("Invalid pagination parameters: page={}, size={}", page, size);
            return ResponseEntity.badRequest().build();
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<UserReport> report = adminReportService.generateUserReport(pageable);
        
        log.info("User report generated with {} users", report.getTotalElements());
        
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/top-customers")
    @Operation(summary = "Get top customers by spending")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Top customers retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid limit parameter"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<UserReport>> getTopCustomers(
        @Parameter(description = "Number of top customers to retrieve (1-100)", example = "10")
        @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Fetching top {} customers by spending", limit);
        
        if (limit < 1 || limit > 100) {
            log.warn("Invalid limit parameter: {}", limit);
            return ResponseEntity.badRequest().build();
        }
        
        List<UserReport> customers = adminReportService.getTopCustomers(limit);
        
        log.info("Retrieved {} top customers", customers.size());
        
        return ResponseEntity.ok(customers);
    }
    
    @GetMapping("/popular-vehicles")
    @Operation(summary = "Get most rented vehicles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Popular vehicles retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid limit parameter"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<VehicleReport>> getMostRentedVehicles(
        @Parameter(description = "Number of vehicles to retrieve (1-100)", example = "10")
        @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Fetching top {} most rented vehicles", limit);
        
        if (limit < 1 || limit > 100) {
            log.warn("Invalid limit parameter: {}", limit);
            return ResponseEntity.badRequest().build();
        }
        
        List<VehicleReport> vehicles = adminReportService.getMostRentedVehicles(limit);
        
        log.info("Retrieved {} most rented vehicles", vehicles.size());
        
        return ResponseEntity.ok(vehicles);
    }
    
    @GetMapping("/export/revenue")
    @Operation(summary = "Export revenue report as CSV/PDF")
    public ResponseEntity<?> exportRevenueReport(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @Parameter(description = "Export format: CSV or PDF")
        @RequestParam(defaultValue = "CSV") String format) {
        
        log.info("Exporting revenue report as {} from {} to {}", format, startDate, endDate);
        
        if (!format.equalsIgnoreCase("CSV") && !format.equalsIgnoreCase("PDF")) {
            log.warn("Invalid export format: {}", format);
            return ResponseEntity.badRequest().body("Supported formats: CSV, PDF");
        }
        
        // Implementation would generate file here
        return ResponseEntity.ok("Export functionality would be implemented");
    }
}
