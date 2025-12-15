package com.carrental.dto.request;

import com.carrental.entity.enums.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {
    
    @NotNull(message = "Rental ID is required")
    private Long rentalId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    @NotBlank(message = "Transaction ID is required")
    @Size(min = 5, max = 50, message = "Transaction ID must be between 5 and 50 characters")
    private String transactionId;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
