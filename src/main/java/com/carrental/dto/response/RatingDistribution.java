package com.carrental.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingDistribution {
    
    private Long vehicleId;
    private Double averageRating;
    private Long totalReviews;
    private Long fiveStarCount;
    private Long fourStarCount;
    private Long threeStarCount;
    private Long twoStarCount;
    private Long oneStarCount;
    
    public Double getFiveStarPercentage() {
        return totalReviews > 0 ? (fiveStarCount * 100.0) / totalReviews : 0.0;
    }
    
    public Double getFourStarPercentage() {
        return totalReviews > 0 ? (fourStarCount * 100.0) / totalReviews : 0.0;
    }
    
    public Double getThreeStarPercentage() {
        return totalReviews > 0 ? (threeStarCount * 100.0) / totalReviews : 0.0;
    }
    
    public Double getTwoStarPercentage() {
        return totalReviews > 0 ? (twoStarCount * 100.0) / totalReviews : 0.0;
    }
    
    public Double getOneStarPercentage() {
        return totalReviews > 0 ? (oneStarCount * 100.0) / totalReviews : 0.0;
    }
}
