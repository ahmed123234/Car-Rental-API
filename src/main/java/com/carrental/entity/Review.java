// ==== Review Entity ====
package com.carrental.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import com.carrental.entity.enums.ReviewStatus;

@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_vehicle_id", columnList = "vehicle_id"),
    @Index(name = "idx_rating", columnList = "rating"),
    @Index(name = "idx_status", columnList = "status"),    
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_rental_id", columnList = "rental_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"vehicle", "user"})
@ToString(exclude = {"vehicle", "user"})
public class Review extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @NotNull
    private Vehicle vehicle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;
    
    @Column(nullable = false)
    @Min(1)
    @Max(5)
    @NotNull
    private Integer rating;
    
    @Column(nullable = false, length = 255)
    @NotBlank
    private String title;
    
    @Column(columnDefinition = "TEXT")
    @Size(max = 1000)
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;
        
    // Aspect ratings (1-5 stars each)
    @Column(name = "vehicle_condition_rating")
    private Integer vehicleConditionRating;
    
    @Column(name = "cleanliness_rating")
    private Integer cleanlinessRating;
    
    @Column(name = "pickup_process_rating")
    private Integer pickupProcessRating;
    
    @Column(name = "return_process_rating")
    private Integer returnProcessRating;
    
    @Column(nullable = false, name = "helpful_count")
    private Long helpfulCount;
    
    @Column(nullable = false)
    private Long unhelpfulCount;
    
    @Column(length = 500)
    private String flagReason;
    
    
    @Version
    private Long version;
}

