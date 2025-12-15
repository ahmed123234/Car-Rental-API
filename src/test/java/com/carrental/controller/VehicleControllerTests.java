package com.carrental.controller;

import com.carrental.dto.request.CreateVehicleRequest;
import com.carrental.entity.enums.TransmissionType;
import com.carrental.entity.enums.VehicleType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class VehicleControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllVehicles() throws Exception {
        mockMvc.perform(get("/vehicles")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateVehicleSuccess() throws Exception {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .licensePlate("NEW123")
                .vin("WVWZZZ3CZ9E111111")
                .make("Toyota")
                .model("Camry")
                .year(2024)
                .color("White")
                .transmission(TransmissionType.AUTOMATIC)
                .dailyRate(new BigDecimal("75.00"))
                .type(VehicleType.SEDAN)
                .build();

        mockMvc.perform(post("/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("VEHICLE_CREATED"))
                .andExpect(jsonPath("$.data.license_plate").value("NEW123"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreateVehicleUnauthorized() throws Exception {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .licensePlate("NEW456")
                .vin("WVWZZZ3CZ9E222222")
                .make("Honda")
                .model("Civic")
                .year(2023)
                .transmission(TransmissionType.MANUAL)
                .dailyRate(new BigDecimal("50.00"))
                .type(VehicleType.SEDAN)
                .build();

        mockMvc.perform(post("/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateVehicleInvalidInput() throws Exception {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .licensePlate("")
                .vin("SHORT")
                .make("Toyota")
                .year(2024)
                .transmission(TransmissionType.AUTOMATIC)
                .dailyRate(new BigDecimal("75.00"))
                .type(VehicleType.SEDAN)
                .build();

        mockMvc.perform(post("/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetVehiclesByType() throws Exception {
        mockMvc.perform(get("/vehicles/type/SEDAN")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetVehiclesByPrice() throws Exception {
        mockMvc.perform(get("/vehicles/price")
                .param("minPrice", "50")
                .param("maxPrice", "100")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}