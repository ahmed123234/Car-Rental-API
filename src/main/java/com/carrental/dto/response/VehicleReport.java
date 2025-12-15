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
public class VehicleReport {
    
    private Long vehicleId;
    private String vehicleMakeModel;
    private String licensePlate;
    
    private Long totalRentals;
    private Long completedRentals;
    private Long cancelledRentals;
    
    private BigDecimal totalRevenue;
    private BigDecimal averageRating;
    private Long reviewCount;
    
    private Long daysInService;
    private Long daysMaintenance;
    private Double utilization; // percentage
    
    private BigDecimal currentValue;
    private Long maintenanceCost;
    
    private String status; // ACTIVE, MAINTENANCE, RESERVED
}


