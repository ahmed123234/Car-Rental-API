package com.carrental.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRentalRequest {
    
    @FutureOrPresent(message = "Pickup date must be in the future")
    private LocalDateTime pickupDate;
    
    @Future(message = "Return date must be in the future")
    private LocalDateTime returnDate;
    
    @Size(min = 2, max = 100, message = "Pickup location must be between 2 and 100 characters")
    private String pickupLocation;
    
    @Size(min = 2, max = 100, message = "Return location must be between 2 and 100 characters")
    private String returnLocation;
    
    @Size(max = 500, message = "Special requests must not exceed 500 characters")
    private String specialRequests;
}
