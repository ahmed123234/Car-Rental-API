package com.carrental.dto.request;

import com.carrental.entity.enums.TransmissionType;
import com.carrental.entity.enums.VehicleType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Create Vehicle Request DTO
 * Contains vehicle information required for vehicle creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVehicleRequest {

    @NotBlank(message = "License plate is required")
    @Size(min = 3, max = 50, message = "License plate must be between 3 and 50 characters")
    private String licensePlate;

    @NotBlank(message = "VIN is required")
    @Size(min = 17, max = 17, message = "VIN must be exactly 17 characters")
    private String vin;

    @NotBlank(message = "Make is required")
    @Size(min = 2, max = 100, message = "Make must be between 2 and 100 characters")
    private String make;

    @NotBlank(message = "Model is required")
    @Size(min = 2, max = 100, message = "Model must be between 2 and 100 characters")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be 1900 or later")
    @Max(value = 2100, message = "Year must be 2100 or earlier")
    private Integer year;

    @Size(max = 50, message = "Color must be maximum 50 characters")
    private String color;

    @NotNull(message = "Transmission type is required")
    private TransmissionType transmission;

    @NotNull(message = "Daily rate is required")
    @DecimalMin(value = "0.01", message = "Daily rate must be greater than 0")
    @DecimalMax(value = "10000.00", message = "Daily rate must be less than 10000")
    private BigDecimal dailyRate;

    @NotNull(message = "Vehicle type is required")
    private VehicleType type;
}