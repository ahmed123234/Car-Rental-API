package com.carrental.controller;

import com.carrental.dto.request.CreateReviewRequest;
import com.carrental.dto.response.ReviewResponse;
import com.carrental.entity.enums.ReviewStatus;
import com.carrental.security.JwtTokenProvider;
import com.carrental.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReviewControllerTests {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ReviewService reviewService;
    
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    
    private String validToken;
    private ReviewResponse testReviewResponse;
    
    @BeforeEach
    void setUp() {
        validToken = "Bearer valid-jwt-token";
        
        testReviewResponse = ReviewResponse.builder()
            .id(1L)
            .vehicleId(1L)
            .userId(1L)
            .rentalId(1L)
            .rating(5)
            .title("Excellent experience")
            .content("Great car and service")
            .status(ReviewStatus.PENDING)
            .helpfulCount(0L)
            .unhelpfulCount(0L)
            .build();
    }
    
    @Test
    void testSubmitReview_Success() throws Exception {
        CreateReviewRequest request = CreateReviewRequest.builder()
            .rentalId(1L)
            .rating(5)
            .title("Excellent experience")
            .content("Great car and service")
            .build();
        
        when(jwtTokenProvider.getUserIdFromToken(anyString())).thenReturn(1L);
        when(reviewService.submitReview(eq(1L), any(CreateReviewRequest.class)))
            .thenReturn(testReviewResponse);
        
        mockMvc.perform(post("/api/reviews")
            .header("Authorization", validToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.rating").value(5));
    }
    
    @Test
    void testGetReview_Success() throws Exception {
        when(reviewService.getReviewById(1L)).thenReturn(testReviewResponse);
        
        mockMvc.perform(get("/api/reviews/1")
            .header("Authorization", validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L));
    }
}
