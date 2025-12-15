package com.carrental.service;

import com.carrental.dto.request.CreateReviewRequest;
import com.carrental.dto.request.UpdateReviewRequest;
import com.carrental.dto.response.RatingDistribution;
import com.carrental.dto.response.ReviewResponse;
import com.carrental.entity.Review;
import com.carrental.entity.Rental;
import com.carrental.entity.User;
import com.carrental.entity.Vehicle;
import com.carrental.entity.enums.ReviewStatus;
import com.carrental.entity.enums.RentalStatus;
import com.carrental.exception.DuplicateReviewException;
import com.carrental.exception.ReviewException;
import com.carrental.mapper.ReviewMapper;
import com.carrental.repository.ReviewRepository;
import com.carrental.repository.RentalRepository;
import com.carrental.repository.UserRepository;
import com.carrental.repository.VehicleRepository;
import com.carrental.util.ReviewUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ReviewMapper reviewMapper;
    
    private static final long REVIEW_SUBMISSION_DEADLINE_DAYS = 30;
    
    /**
     * Submit a review for a rental
     */
    public ReviewResponse submitReview(Long userId, CreateReviewRequest request) {
        log.info("Submitting review for rental: {}, user: {}", request.getRentalId(), userId);
        
        // Fetch rental
        Rental rental = rentalRepository.findById(request.getRentalId())
            .orElseThrow(() -> new ReviewException("Rental not found"));
        
        // Validate user owns the rental
        if (!rental.getUser().getId().equals(userId)) {
            throw new ReviewException("Unauthorized: Cannot review rental belonging to another user");
        }
        
        // Check if rental is completed
        if (rental.getStatus() != RentalStatus.COMPLETED) {
            throw new ReviewException("Can only review completed rentals");
        }
        
        // Check if review already exists for this rental
        if (reviewRepository.findByRentalId(rental.getId()).isPresent()) {
            throw new DuplicateReviewException("Review already exists for this rental");
        }
        
        // Check if within review submission window (30 days after completion)
        long daysSinceCompletion = ChronoUnit.DAYS.between(rental.getUpdatedAt(), LocalDateTime.now());
        if (daysSinceCompletion > REVIEW_SUBMISSION_DEADLINE_DAYS) {
            throw new ReviewException("Review submission deadline has passed (30 days after rental completion)");
        }
        
        // Validate review content
        if (!ReviewUtil.isValidReviewContent(request.getTitle(), request.getContent())) {
            throw new ReviewException("Invalid review content");
        }
        
        // Check for profanity
        if (ReviewUtil.containsProfanity(request.getTitle()) || ReviewUtil.containsProfanity(request.getContent())) {
            throw new ReviewException("Review contains inappropriate content");
        }
        
        // Fetch vehicle
        Vehicle vehicle = vehicleRepository.findById(rental.getVehicle().getId())
            .orElseThrow(() -> new ReviewException("Vehicle not found"));
        
        // Fetch user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ReviewException("User not found"));
        
        // Create review
        Review review = Review.builder()
            .vehicle(vehicle)
            .user(user)
            .rental(rental)
            .rating(request.getRating())
            .title(ReviewUtil.sanitizeContent(request.getTitle()))
            .content(ReviewUtil.sanitizeContent(request.getContent()))
            .vehicleConditionRating(request.getVehicleConditionRating())
            .cleanlinessRating(request.getCleanlinessRating())
            .pickupProcessRating(request.getPickupProcessRating())
            .returnProcessRating(request.getReturnProcessRating())
            .status(ReviewStatus.PENDING)
            .helpfulCount(0L)
            .unhelpfulCount(0L)
            .build();
        
        Review savedReview = reviewRepository.save(review);
        log.info("Review submitted successfully with ID: {}", savedReview.getId());
        
        return reviewMapper.toResponse(savedReview);
    }
    
    /**
     * Get all reviews for a vehicle (approved only)
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getVehicleReviews(Long vehicleId, Pageable pageable) {
        log.debug("Fetching reviews for vehicle: {}", vehicleId);
        
        return reviewRepository.findByVehicleIdAndStatus(vehicleId, ReviewStatus.APPROVED, pageable)
            .map(reviewMapper::toResponse);
    }
    
    /**
     * Get most helpful reviews for a vehicle
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMostHelpfulReviews(Long vehicleId, Pageable pageable) {
        log.debug("Fetching most helpful reviews for vehicle: {}", vehicleId);
        
        return reviewRepository.findMostHelpfulReviewsByVehicle(vehicleId, pageable)
            .map(reviewMapper::toResponse);
    }
    
    /**
     * Get recent reviews for a vehicle
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getRecentReviews(Long vehicleId, Pageable pageable) {
        log.debug("Fetching recent reviews for vehicle: {}", vehicleId);
        
        return reviewRepository.findRecentReviewsByVehicle(vehicleId, pageable)
            .map(reviewMapper::toResponse);
    }
    
    /**
     * Get all reviews by a user
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getUserReviews(Long userId) {
        log.debug("Fetching reviews by user: {}", userId);
        
        return reviewRepository.findByUserId(userId).stream()
            .map(reviewMapper::toResponse)
            .toList();
    }
    
    /**
     * Get review by ID
     */
    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(Long reviewId) {
        log.debug("Fetching review: {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewException("Review not found with ID: " + reviewId));
        
        return reviewMapper.toResponse(review);
    }
    
    /**
     * Update a review
     */
    public ReviewResponse updateReview(Long reviewId, Long userId, UpdateReviewRequest request) {
        log.info("Updating review: {} for user: {}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewException("Review not found"));
        
        if (!review.getUser().getId().equals(userId)) {
            throw new ReviewException("Unauthorized: Cannot update review belonging to another user");
        }
        
        if (review.getStatus() == ReviewStatus.DELETED) {
            throw new ReviewException("Cannot update a deleted review");
        }
        
        // Update fields if provided
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        
        if (request.getTitle() != null) {
            if (ReviewUtil.containsProfanity(request.getTitle())) {
                throw new ReviewException("Updated title contains inappropriate content");
            }
            review.setTitle(ReviewUtil.sanitizeContent(request.getTitle()));
        }
        
        if (request.getContent() != null) {
            if (ReviewUtil.containsProfanity(request.getContent())) {
                throw new ReviewException("Updated content contains inappropriate content");
            }
            review.setContent(ReviewUtil.sanitizeContent(request.getContent()));
        }
        
        if (request.getVehicleConditionRating() != null) {
            review.setVehicleConditionRating(request.getVehicleConditionRating());
        }
        
        if (request.getCleanlinessRating() != null) {
            review.setCleanlinessRating(request.getCleanlinessRating());
        }
        
        if (request.getPickupProcessRating() != null) {
            review.setPickupProcessRating(request.getPickupProcessRating());
        }
        
        if (request.getReturnProcessRating() != null) {
            review.setReturnProcessRating(request.getReturnProcessRating());
        }
        
        // Reset status to pending after update
        review.setStatus(ReviewStatus.PENDING);
        
        Review updatedReview = reviewRepository.save(review);
        log.info("Review updated successfully: {}", reviewId);
        
        return reviewMapper.toResponse(updatedReview);
    }
    
    /**
     * Delete a review (soft delete)
     */
    public void deleteReview(Long reviewId, Long userId) {
        log.info("Deleting review: {} for user: {}", reviewId, userId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewException("Review not found"));
        
        if (!review.getUser().getId().equals(userId)) {
            throw new ReviewException("Unauthorized: Cannot delete review belonging to another user");
        }
        
        review.setStatus(ReviewStatus.DELETED);
        reviewRepository.save(review);
        
        log.info("Review flagged successfully: {}", reviewId);
    }
    
    /**
     * Approve a review (admin only)
     */
    public ReviewResponse approveReview(Long reviewId) {
        log.info("Approving review: {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewException("Review not found"));
        
        if (review.getStatus() == ReviewStatus.DELETED) {
            throw new ReviewException("Cannot approve a deleted review");
        }
        
        review.setStatus(ReviewStatus.APPROVED);
        review.setFlagReason(null);
        
        Review approvedReview = reviewRepository.save(review);
        log.info("Review approved successfully: {}", reviewId);
        
        return reviewMapper.toResponse(approvedReview);
    }
    
    /**
     * Reject a review (admin only)
     */
    public ReviewResponse rejectReview(Long reviewId, String reason) {
        log.info("Rejecting review: {} with reason: {}", reviewId, reason);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewException("Review not found"));
        
        review.setStatus(ReviewStatus.REJECTED);
        review.setFlagReason(reason);
        
        Review rejectedReview = reviewRepository.save(review);
        log.info("Review rejected successfully: {}", reviewId);
        
        return reviewMapper.toResponse(rejectedReview);
    }
    
    /**
     * Mark review as helpful
     */
    public void markAsHelpful(Long reviewId) {
        log.debug("Marking review {} as helpful", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewException("Review not found"));
        
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        reviewRepository.save(review);
    }
    
    /**
     * Mark review as unhelpful
     */
    public void markAsUnhelpful(Long reviewId) {
        log.debug("Marking review {} as unhelpful", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewException("Review not found"));
        
        review.setUnhelpfulCount(review.getUnhelpfulCount() + 1);
        reviewRepository.save(review);
    }
    
    /**
     * Get flagged reviews for admin
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getFlaggedReviews() {
        log.debug("Fetching flagged reviews");
        
        return reviewRepository.findByStatus(ReviewStatus.FLAGGED).stream()
            .map(reviewMapper::toResponse)
            .toList();
    }
    
    /**
     * Get pending reviews for moderation
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getPendingReviews() {
        log.debug("Fetching pending reviews");
        
        return reviewRepository.findByStatus(ReviewStatus.PENDING).stream()
            .map(reviewMapper::toResponse)
            .toList();
    }
}

