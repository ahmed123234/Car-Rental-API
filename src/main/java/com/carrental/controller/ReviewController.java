package com.carrental.controller;

import com.carrental.dto.request.CreateReviewRequest;
import com.carrental.dto.request.UpdateReviewRequest;
import com.carrental.dto.response.RatingDistribution;
import com.carrental.dto.response.ReviewResponse;
import com.carrental.security.JwtTokenProvider;
import com.carrental.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Review Management", description = "APIs for managing vehicle reviews and ratings")
@SecurityRequirement(name = "Bearer Authentication")
public class ReviewController {
    
    private final ReviewService reviewService;
    private final JwtTokenProvider jwtTokenProvider;
    
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Submit a review for a completed rental")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Review submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid review data"),
        @ApiResponse(responseCode = "404", description = "Rental not found"),
        @ApiResponse(responseCode = "409", description = "Review already exists or rental not completed")
    })
    public ResponseEntity<ReviewResponse> submitReview(
        @Valid @RequestBody CreateReviewRequest request,
        @RequestHeader("Authorization") String token) {
        
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        log.info("Submitting review for rental: {}", request.getRentalId());
        
        ReviewResponse response = reviewService.submitReview(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get all reviews for a vehicle")
    public ResponseEntity<Page<ReviewResponse>> getVehicleReviews(
        @Parameter(description = "Vehicle ID") @PathVariable Long vehicleId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewResponse> response = reviewService.getVehicleReviews(vehicleId, pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/vehicle/{vehicleId}/helpful")
    @Operation(summary = "Get most helpful reviews for a vehicle")
    public ResponseEntity<Page<ReviewResponse>> getMostHelpfulReviews(
        @Parameter(description = "Vehicle ID") @PathVariable Long vehicleId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse> response = reviewService.getMostHelpfulReviews(vehicleId, pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/vehicle/{vehicleId}/recent")
    @Operation(summary = "Get recent reviews for a vehicle")
    public ResponseEntity<Page<ReviewResponse>> getRecentReviews(
        @Parameter(description = "Vehicle ID") @PathVariable Long vehicleId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse> response = reviewService.getRecentReviews(vehicleId, pageable);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get review details by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Review found"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ReviewResponse> getReview(
        @Parameter(description = "Review ID") @PathVariable Long id) {
        
        ReviewResponse response = reviewService.getReviewById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/my-reviews")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get all reviews submitted by current user")
    public ResponseEntity<List<ReviewResponse>> getUserReviews(
        @RequestHeader("Authorization") String token) {
        
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        List<ReviewResponse> response = reviewService.getUserReviews(userId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Update a review")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Review updated successfully"),
        @ApiResponse(responseCode = "404", description = "Review not found"),
        @ApiResponse(responseCode = "400", description = "Invalid update data")
    })
    public ResponseEntity<ReviewResponse> updateReview(
        @Parameter(description = "Review ID") @PathVariable Long id,
        @Valid @RequestBody UpdateReviewRequest request,
        @RequestHeader("Authorization") String token) {
        
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        log.info("Updating review: {} for user: {}", id, userId);
        
        ReviewResponse response = reviewService.updateReview(id, userId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Delete a review")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Review deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<Void> deleteReview(
        @Parameter(description = "Review ID") @PathVariable Long id,
        @RequestHeader("Authorization") String token) {
        
        Long userId = jwtTokenProvider.getUserIdFromToken(token.substring(7));
        log.info("Deleting review: {} for user: {}", id, userId);
        
        reviewService.deleteReview(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/vehicle/{vehicleId}/rating")
    @Operation(summary = "Get average rating for a vehicle")
    public ResponseEntity<Double> getAverageRating(
        @Parameter(description = "Vehicle ID") @PathVariable Long vehicleId) {
        
        Double averageRating = reviewService.getAverageRating(vehicleId);
        return ResponseEntity.ok(averageRating);
    }
    
    @GetMapping("/vehicle/{vehicleId}/distribution")
    @Operation(summary = "Get rating distribution for a vehicle")
    public ResponseEntity<RatingDistribution> getRatingDistribution(
        @Parameter(description = "Vehicle ID") @PathVariable Long vehicleId) {
        
        RatingDistribution distribution = reviewService.getRatingDistribution(vehicleId);
        return ResponseEntity.ok(distribution);
    }
    
    @PostMapping("/{id}/helpful")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Mark review as helpful")
    public ResponseEntity<Void> markAsHelpful(
        @Parameter(description = "Review ID") @PathVariable Long id) {
        
        reviewService.markAsHelpful(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/unhelpful")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Mark review as unhelpful")
    public ResponseEntity<Void> markAsUnhelpful(
        @Parameter(description = "Review ID") @PathVariable Long id) {
        
        reviewService.markAsUnhelpful(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/flag")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Flag review for inappropriate content")
    public ResponseEntity<Void> flagReview(
        @Parameter(description = "Review ID") @PathVariable Long id,
        @RequestParam String reason) {
        
        reviewService.flagReview(id, reason);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a review (Admin only)")
    public ResponseEntity<ReviewResponse> approveReview(
        @Parameter(description = "Review ID") @PathVariable Long id) {
        
        log.info("Approving review: {}", id);
        ReviewResponse response = reviewService.approveReview(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject a review (Admin only)")
    public ResponseEntity<ReviewResponse> rejectReview(
        @Parameter(description = "Review ID") @PathVariable Long id,
        @RequestParam String reason) {
        
        log.info("Rejecting review: {}", id);
        ReviewResponse response = reviewService.rejectReview(id, reason);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/admin/flagged")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get flagged reviews (Admin only)")
    public ResponseEntity<List<ReviewResponse>> getFlaggedReviews() {
        List<ReviewResponse> response = reviewService.getFlaggedReviews();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get pending reviews for moderation (Admin only)")
    public ResponseEntity<List<ReviewResponse>> getPendingReviews() {
        List<ReviewResponse> response = reviewService.getPendingReviews();
        return ResponseEntity.ok(response);
    }
}
