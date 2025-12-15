package com.carrental.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewRequest {
    
    @NotNull(message = "Rental ID is required")
    private Long rentalId;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;
    
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;
    
    @Size(max = 1000, message = "Content cannot exceed 1000 characters")
    private String content;
    
    @Min(value = 1, message = "Vehicle condition rating must be between 1 and 5")
    @Max(value = 5, message = "Vehicle condition rating must be between 1 and 5")
    private Integer vehicleConditionRating;
    
    @Min(value = 1, message = "Cleanliness rating must be between 1 and 5")
    @Max(value = 5, message = "Cleanliness rating must be between 1 and 5")
    private Integer cleanlinessRating;
    
    @Min(value = 1, message = "Pickup process rating must be between 1 and 5")
    @Max(value = 5, message = "Pickup process rating must be between 1 and 5")
    private Integer pickupProcessRating;
    
    @Min(value = 1, message = "Return process rating must be between 1 and 5")
    @Max(value = 5, message = "Return process rating must be between 1 and 5")
    private Integer returnProcessRating;
}
