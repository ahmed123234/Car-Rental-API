// ==== Payment Entity ====
package com.carrental.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import com.carrental.entity.enums.PaymentStatus;
import com.carrental.entity.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedAt;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_rental_id", columnList = "rental_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_transaction_id", columnList = "transaction_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"rental"})
@ToString(exclude = {"rental"})
public class Payment extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    @NotNull
    private Rental rental;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    
    @Column(nullable = false, precision = 10, scale = 2)
    @DecimalMin("0.01")
    @NotNull
    private BigDecimal amount;


    @Column(precision = 10, scale = 2)
    private BigDecimal refundedAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(name = "transaction_id", length = 255)
    private String transactionId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(length = 500)
    private String description;

    @CreatedAt
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
}
