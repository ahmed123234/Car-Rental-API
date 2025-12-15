package com.carrental.dto.response;

import com.carrental.entity.enums.TransmissionType;
import com.carrental.entity.enums.VehicleStatus;
import com.carrental.entity.enums.VehicleType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Vehicle Data Transfer Object
 * Contains vehicle information for API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDTO {

    private Long id;

    @JsonProperty("license_plate")
    private String licensePlate;

    private String vin;

    private String make;

    private String model;

    private Integer year;

    private String color;

    private TransmissionType transmission;

    @JsonProperty("daily_rate")
    private BigDecimal dailyRate;

    private VehicleType type;

    private VehicleStatus status;

    private Integer mileage;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}