package com.carrental.dto.response;

import com.carrental.entity.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    
    private Long id;
    private Long vehicleId;
    private String vehicleMakeModel;
    private Long userId;
    private String userName;
    private Long rentalId;
    private Integer rating;
    private String title;
    private String content;
    private Integer vehicleConditionRating;
    private Integer cleanlinessRating;
    private Integer pickupProcessRating;
    private Integer returnProcessRating;
    private ReviewStatus status;
    private Long helpfulCount;
    private Long unhelpfulCount;
    private String flagReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
