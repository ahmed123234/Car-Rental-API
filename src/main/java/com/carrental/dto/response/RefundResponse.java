package com.carrental.dto.response;

import com.carrental.entity.enums.RefundStatus;
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
public class RefundResponse {
    
    private Long id;
    private Long paymentId;
    private Long rentalId;
    private BigDecimal amount;
    private RefundStatus status;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
