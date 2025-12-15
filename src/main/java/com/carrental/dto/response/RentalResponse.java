package com.carrental.dto.response;

import com.carrental.entity.enums.RentalStatus;
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
public class RentalResponse {
    
    private Long id;
    private Long userId;
    private Long vehicleId;
    private String vehicleMakeModel;
    private LocalDateTime pickupDate;
    private LocalDateTime returnDate;
    private String pickupLocation;
    private String returnLocation;
    private BigDecimal dailyRate;
    private BigDecimal totalCost;
    private BigDecimal additionalFees;
    private RentalStatus status;
    private String specialRequests;
    private Long rentalDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
