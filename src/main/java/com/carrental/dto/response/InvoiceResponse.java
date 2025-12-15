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
public class InvoiceResponse {
    
    private Long id;
    private Long rentalId;
    private String invoiceNumber;
    private String vehicleMakeModel;
    private String rentalPeriod;
    private BigDecimal dailyRate;
    private Long rentalDays;
    private BigDecimal subtotal;
    private BigDecimal taxes;
    private BigDecimal discount;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime createdAt;
}