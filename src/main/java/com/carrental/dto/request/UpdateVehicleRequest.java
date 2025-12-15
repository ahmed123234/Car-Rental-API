package com.carrental.dto.request;

import com.carrental.entity.enums.VehicleStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Update Vehicle Request DTO
 * Contains updateable vehicle information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVehicleRequest {

    @Size(max = 50, message = "Color must be maximum 50 characters")
    private String color;

    @DecimalMin(value = "0.01", message = "Daily rate must be greater than 0")
    @DecimalMax(value = "10000.00", message = "Daily rate must be less than 10000")
    private BigDecimal dailyRate;

    private VehicleStatus status;

    @Min(value = 0, message = "Mileage cannot be negative")
    @Max(value = 1000000, message = "Mileage must be less than 1,000,000")
    private Integer mileage;
}