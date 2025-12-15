package com.carrental.dto.response;

import com.carrental.entity.enums.PaymentMethod;
import com.carrental.entity.enums.PaymentStatus;
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
public class PaymentResponse {
    
    private Long id;
    private Long rentalId;
    private Long userId;
    private BigDecimal amount;
    private BigDecimal refundedAmount;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private PaymentStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}