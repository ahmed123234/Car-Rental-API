package com.carrental.mapper;

import com.carrental.dto.response.PaymentResponse;
import com.carrental.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentMapper {
    
    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        
        return PaymentResponse.builder()
            .id(payment.getId())
            .rentalId(payment.getRental().getId())
            .userId(payment.getUser().getId())
            .amount(payment.getAmount())
            .refundedAmount(payment.getRefundedAmount())
            .paymentMethod(payment.getPaymentMethod())
            .transactionId(payment.getTransactionId())
            .status(payment.getStatus())
            .description(payment.getDescription())
            .createdAt(payment.getCreatedAt())
            .updatedAt(payment.getUpdatedAt())
            .build();
    }
}
