package com.carrental.controller;

import com.carrental.dto.request.CreateRentalRequest;
import com.carrental.dto.response.RentalResponse;
import com.carrental.entity.enums.RentalStatus;
import com.carrental.security.JwtTokenProvider;
import com.carrental.service.RentalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RentalControllerTests {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private RentalService rentalService;
    
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    
    private String validToken;
    private RentalResponse testRentalResponse;
    
    @BeforeEach
    void setUp() {
        validToken = "Bearer valid-jwt-token";
        
        testRentalResponse = RentalResponse.builder()
            .id(1L)
            .userId(1L)
            .vehicleId(1L)
            .vehicleMakeModel("Toyota Camry")
            .pickupDate(LocalDateTime.now().plusDays(2))
            .returnDate(LocalDateTime.now().plusDays(5))
            .pickupLocation("Airport")
            .returnLocation("Downtown")
            .dailyRate(BigDecimal.valueOf(50))
            .totalCost(BigDecimal.valueOf(150))
            .status(RentalStatus.PENDING)
            .rentalDays(3L)
            .build();
    }
    
    @Test
    void testCreateRental_Success() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        CreateRentalRequest request = CreateRentalRequest.builder()
            .vehicleId(1L)
            .pickupDate(now.plusDays(2))
            .returnDate(now.plusDays(5))
            .pickupLocation("Airport")
            .returnLocation("Downtown")
            .build();
        
        when(jwtTokenProvider.getUserIdFromToken(anyString())).thenReturn(1L);
        when(rentalService.createRental(eq(1L), any(CreateRentalRequest.class)))
            .thenReturn(testRentalResponse);
        
        mockMvc.perform(post("/api/rentals")
            .header("Authorization", validToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }
    
    @Test
    void testGetRental_Success() throws Exception {
        when(rentalService.getRentalById(1L)).thenReturn(testRentalResponse);
        
        mockMvc.perform(get("/api/rentals/1")
            .header("Authorization", validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L));
    }
    
    @Test
    void testCancelRental_Success() throws Exception {
        testRentalResponse.setStatus(RentalStatus.CANCELLED);
        
        when(jwtTokenProvider.getUserIdFromToken(anyString())).thenReturn(1L);
        when(rentalService.cancelRental(1L, 1L)).thenReturn(testRentalResponse);
        
        mockMvc.perform(delete("/api/rentals/1")
            .header("Authorization", validToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}

