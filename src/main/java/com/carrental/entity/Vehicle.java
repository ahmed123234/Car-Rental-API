// ==== Vehicle Entity ====
package com.carrental.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import com.carrental.entity.enums.VehicleType;
import com.carrental.entity.enums.VehicleStatus;
import com.carrental.entity.enums.TransmissionType;
import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "vehicles", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_daily_rate", columnList = "daily_rate")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"rentals", "reviews"})
@ToString(exclude = {"rentals", "reviews"})
public class Vehicle extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    @NotBlank
    private String licensePlate;
    
    @Column(unique = true, nullable = false, length = 100)
    @NotBlank
    private String vin;
    
    @Column(nullable = false, length = 100)
    @NotBlank
    private String make;
    
    @Column(nullable = false, length = 100)
    @NotBlank
    private String model;
    
    @Column(nullable = false)
    @Min(1900)
    @Max(2100)
    private Integer year;
    
    @Column(length = 50)
    private String color;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransmissionType transmission;
    
    @Column(name = "daily_rate", nullable = false, precision = 10, scale = 2)
    @DecimalMin("0.01")
    @NotNull
    private BigDecimal dailyRate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.ACTIVE;
    
    @Column(nullable = false)
    @Min(0)
    @Builder.Default
    private Integer mileage = 0;
    
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Rental> rentals;
    
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Review> reviews;
}