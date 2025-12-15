package com.carrental.mapper;

import com.carrental.dto.response.ReviewResponse;
import com.carrental.entity.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMapper {
    
    public ReviewResponse toResponse(Review review) {
        if (review == null) {
            return null;
        }
        
        return ReviewResponse.builder()
            .id(review.getId())
            .vehicleId(review.getVehicle().getId())
            .vehicleMakeModel(review.getVehicle().getMake() + " " + review.getVehicle().getModel())
            .userId(review.getUser().getId())
            .userName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
            .rentalId(review.getRental().getId())
            .rating(review.getRating())
            .title(review.getTitle())
            .content(review.getContent())
            .vehicleConditionRating(review.getVehicleConditionRating())
            .cleanlinessRating(review.getCleanlinessRating())
            .pickupProcessRating(review.getPickupProcessRating())
            .returnProcessRating(review.getReturnProcessRating())
            .status(review.getStatus())
            .helpfulCount(review.getHelpfulCount())
            .unhelpfulCount(review.getUnhelpfulCount())
            .flagReason(review.getFlagReason())
            .createdAt(review.getCreatedAt())
            .updatedAt(review.getUpdatedAt())
            .build();
    }
}