package com.carrental.service;

import com.carrental.dto.request.CreateReviewRequest;
import com.carrental.dto.request.UpdateReviewRequest;
import com.carrental.dto.response.RatingDistribution;
import com.carrental.dto.response.ReviewResponse;
import com.carrental.entity.*;
import com.carrental.entity.enums.*;
import com.carrental.exception.DuplicateReviewException;
import com.carrental.exception.ReviewException;
import com.carrental.mapper.ReviewMapper;
import com.carrental.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTests {
    
    @Mock
    private ReviewRepository reviewRepository;
    
    @Mock
    private RentalRepository rentalRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private VehicleRepository vehicleRepository;
    
    @Mock
    private ReviewMapper reviewMapper;
    
    @InjectMocks
    private ReviewService reviewService;
    
    private User testUser;
    private Vehicle testVehicle;
    private Rental testRental;
    private Review testReview;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .email("user@test.com")
            .firstName("John")
            .lastName("Doe")
            .build();
        
        testVehicle = Vehicle.builder()
            .id(1L)
            .make("Toyota")
            .model("Camry")
            .dailyRate(BigDecimal.valueOf(50))
            .build();
        
        LocalDateTime now = LocalDateTime.now();
        testRental = Rental.builder()
            .id(1L)
            .user(testUser)
            .vehicle(testVehicle)
            .pickupDate(now.minusDays(5))
            .returnDate(now.minusDays(2))
            .totalCost(BigDecimal.valueOf(150))
            .status(RentalStatus.COMPLETED)
            .updatedAt(now.minusDays(1))
            .build();
        
        testReview = Review.builder()
            .id(1L)
            .vehicle(testVehicle)
            .user(testUser)
            .rental(testRental)
            .rating(5)
            .title("Excellent rental experience")
            .content("Great car and service")
            .status(ReviewStatus.APPROVED)
            .helpfulCount(0L)
            .unhelpfulCount(0L)
            .build();
    }
    
    @Test
    void testSubmitReview_Success() {
        CreateReviewRequest request = CreateReviewRequest.builder()
            .rentalId(1L)
            .rating(5)
            .title("Excellent rental experience")
            .content("Great car and service")
            .vehicleConditionRating(5)
            .cleanlinessRating(5)
            .pickupProcessRating(5)
            .returnProcessRating(5)
            .build();
        
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));
        when(reviewRepository.findByRentalId(1L)).thenReturn(Optional.empty());
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(reviewMapper.toResponse(testReview)).thenReturn(ReviewResponse.builder()
            .id(1L)
            .rating(5)
            .status(ReviewStatus.PENDING)
            .build());
        
        ReviewResponse response = reviewService.submitReview(1L, request);
        
        assertNotNull(response);
        assertEquals(5, response.getRating());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }
    
    @Test
    void testSubmitReview_DuplicateReview() {
        CreateReviewRequest request = CreateReviewRequest.builder()
            .rentalId(1L)
            .rating(5)
            .title("Excellent rental experience")
            .content("Great car and service")
            .build();
        
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));
        when(reviewRepository.findByRentalId(1L)).thenReturn(Optional.of(testReview));
        
        assertThrows(DuplicateReviewException.class, () -> reviewService.submitReview(1L, request));
    }
    
    @Test
    void testSubmitReview_RentalNotCompleted() {
        testRental.setStatus(RentalStatus.PENDING);
        CreateReviewRequest request = CreateReviewRequest.builder()
            .rentalId(1L)
            .rating(5)
            .title("Excellent rental experience")
            .build();
        
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));
        
        assertThrows(ReviewException.class, () -> reviewService.submitReview(1L, request));
    }
    
    @Test
    void testGetAverageRating_Success() {
        when(reviewRepository.findAverageRatingByVehicle(1L)).thenReturn(Optional.of(4.5));
        
        Double averageRating = reviewService.getAverageRating(1L);
        
        assertEquals(4.5, averageRating);
    }
    
    @Test
    void testApproveReview_Success() {
        testReview.setStatus(ReviewStatus.PENDING);
        
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(reviewMapper.toResponse(testReview)).thenReturn(ReviewResponse.builder()
            .id(1L)
            .status(ReviewStatus.APPROVED)
            .build());
        
        ReviewResponse response = reviewService.approveReview(1L);
        
        assertNotNull(response);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }
}

