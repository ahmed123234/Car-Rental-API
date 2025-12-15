package com.carrental.integration;

import com.carrental.dto.request.CreateRentalRequest;
import com.carrental.entity.User;
import com.carrental.entity.Vehicle;
import com.carrental.repository.RentalRepository;
import com.carrental.repository.UserRepository;
import com.carrental.repository.VehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class RentalIntegrationTests {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private RentalRepository rentalRepository;
    
    private String authToken;
    private User testUser;
    private Vehicle testVehicle;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .build();
        userRepository.save(testUser);
        
        testVehicle = Vehicle.builder()
            .make("Honda")
            .model("Civic")
            .year(2023)
            .licensePlate("ABC123")
            .dailyRate(BigDecimal.valueOf(45))
            .build();
        vehicleRepository.save(testVehicle);
    }
    
    @Test
    void testCompleteRentalFlow() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        CreateRentalRequest request = CreateRentalRequest.builder()
            .vehicleId(testVehicle.getId())
            .pickupDate(now.plusDays(1))
            .returnDate(now.plusDays(4))
            .pickupLocation("Location A")
            .returnLocation("Location B")
            .build();
        
        mockMvc.perform(post("/api/rentals")
            .header("Authorization", "Bearer " + authToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists());
    }
}