package com.carrental.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import com.carrental.entity.enums.RentalStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.CreatedAt;


@Entity
@Table(name = "rentals", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_vehicle_id", columnList = "vehicle_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_dates", columnList = "pickup_date, return_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "vehicle", "payments"})
@ToString(exclude = {"user", "vehicle", "payments"})
public class Rental extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @NotNull
    private Vehicle vehicle;
    
    @Column(name = "pickup_date", nullable = false)
    @NotNull
    private LocalDateTime pickupDate;
    
    @Column(name = "return_date", nullable = false)
    @NotNull
    private LocalDateTime returnDate;
    
    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate;
    
    @Column(name = "pickup_location", nullable = false, length = 255)
    @NotBlank
    private String pickupLocation;
    
    @Column(name = "return_location", length = 255)
    private String returnLocation;
    
    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    @DecimalMin("0.00")
    @NotNull
    private BigDecimal totalCost;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RentalStatus status = RentalStatus.PENDING;
    
    @Column(name = "special_requests", columnDefinition = "TEXT", length = 2000)
    private String specialRequests;
    
    @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.Set<Payment> payments;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal additionalFees;
    
    @CreatedAt
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
}
